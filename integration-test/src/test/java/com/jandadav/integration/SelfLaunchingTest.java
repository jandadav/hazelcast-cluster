package com.jandadav.integration;

import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SelfLaunchingTest {

    private static Process discoveryProcess;
    private static Process hazelcast1Process;
    private static Process hazelcast2Process;

    @BeforeAll
    static void beforeAll() throws Exception {
        startEureka();
        startHazel1();
    }

    @Test
    @Order(1)
    void eurekaStart() {
        await().atMost(20, SECONDS).until(eurekaIsUp());
    }

    @Test
    @Order(2)
    void hazelcast1Register() {
        await().atMost(60, SECONDS).until(hazelcast1IsRegistered());

        //Tomcat starts last, the Hazelcast-Eureka integration waits for instance retrieval
        await().atMost(60, SECONDS).ignoreExceptions()
                .until(
                        () -> given().get("http://localhost:9090/").statusCode(),
                        equalTo(200)
                );
    }

    @Test
    @Order(3)
    void loadDataToHazel1() {

        // instance not ready here, Tomcat starts last but eureka registers with UP from the
        //  start.

        given().header("Content-Type", "application/x-www-form-urlencoded")
            .formParam("key", "key1")
            .formParam("value", "value1")
            .when()
            .post("http://localhost:9090/put")
            .then()
            .statusCode(200);
        given()
            .formParam("key", "key1")
            .when()
            .get("http://localhost:9090/get")
            .then()
            .statusCode(200)
            .body(containsStringIgnoringCase("value1"));
    }

    @Test
    @Order(4)
    void startHazel2andWaitForRegistration() throws Exception{
        startHazel2();
        await().atMost(60, SECONDS).ignoreExceptions()
                .until(
                () -> given().get("http://localhost:9091/").statusCode(),
                equalTo(200)
        );

    }

    @Test
    @Order(5)
    void retrieveReplicatedDataFromHazel2() {

        // This is very fast replication of 1 record after startup and registration
        await().atMost(1, SECONDS).ignoreExceptions()
                .until(
                        () -> given()
                                .formParam("key", "key1")
                                .when()
                                .get("http://localhost:9091/get")
                                .body(),
                                responseBody -> responseBody.asString().contains("value1"))
                ;
    }

    @Test
    @Order(6)
    void canReplicateInClusterFast() {
        given().header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("key", "key2")
                .formParam("value", "Cyndi Lauper")
                .when()
                .post("http://localhost:9090/put")
                .then()
                .statusCode(200);

        await().atMost(Duration.ofSeconds(1));

        given()
                .formParam("key", "key2")
                .when()
                .get("http://localhost:9091/get")
                .then()
                .statusCode(200)
                .body(containsStringIgnoringCase("Cyndi Lauper"));
    }

    @Disabled
    @Test
    @Order(7)
    void loadLotsOfData() {

        // TODO profile hazelcast vm
        // this loads 16k records before it fails
        //        Loaded: 16000 records
        //        Led 11, 2021 12:01:28 DOP. org.apache.http.impl.client.DefaultHttpClient tryConnect
        //        INFO: I/O exception (java.net.BindException) caught when connecting to {}->http://localhost:9090: Address already in use: connect
        //        Led 11, 2021 12:01:28 DOP. org.apache.http.impl.client.DefaultHttpClient tryConnect

        for(int i = 0; i < 100_000; i++) {
            given().header("Content-Type", "application/x-www-form-urlencoded")
                    .formParam("key", RandomStringUtils.random(30))
                    .formParam("value", RandomStringUtils.random(5000))
                    .when()
                    .post("http://localhost:9090/put")
                    .then()
                    .statusCode(200);
            if (i % 1000 == 0) {
                System.out.println("Loaded: " + i + " records");
            }
        }

    }

    @AfterAll
    public static void afterClass() {
        discoveryProcess.destroy();
        hazelcast1Process.destroy();
        hazelcast2Process.destroy();
    }


    private Callable<Boolean> hazelcast1IsRegistered() {
        return () -> {
            try {
                return given()
                .accept(ContentType.JSON)
                .when()
                .get("http://localhost:8761/eureka/apps")
                .body().asString().contains("hazelcast-node:9090");
            } catch (Exception e) {
                return false;
            }
        };
    }

    private Callable<Boolean> eurekaIsUp() {
        return () -> {
            try {
                return given()
                        .accept(ContentType.JSON)
                        .when()
                        .get("http://localhost:8761/eureka/apps")
                        .statusCode() == 200;
            } catch (Exception e) {
                return false;
            }
        };
    }

    private static void startHazel1() throws IOException {

        if(hazelcast1Process != null) {
            hazelcast1Process.destroy();
        }

        ArrayList<String> hazelcast1Command = new ArrayList<String>();
        hazelcast1Command.add("java");
        hazelcast1Command.add("-Dserver.port=9090");
        hazelcast1Command.add("-jar");
        hazelcast1Command.add("../hazelcast-node/build/libs/hazelcast-node.jar");

        ProcessBuilder builder = new ProcessBuilder(hazelcast1Command);
        hazelcast1Process = builder.inheritIO().start();
    }

    private static void startHazel2() throws IOException {

        if(hazelcast2Process != null) {
            hazelcast2Process.destroy();
        }

        ArrayList<String> hazelcast2Command = new ArrayList<String>();
        hazelcast2Command.add("java");
        hazelcast2Command.add("-Dserver.port=9091");
        hazelcast2Command.add("-jar");
        hazelcast2Command.add("../hazelcast-node/build/libs/hazelcast-node.jar");

        ProcessBuilder builder = new ProcessBuilder(hazelcast2Command);
        hazelcast2Process = builder.inheritIO().start();
    }

    private static void startEureka() throws IOException {

        if(discoveryProcess != null) {
            discoveryProcess.destroy();
        }

        ArrayList<String> discoveryCommand = new ArrayList<String>();
        discoveryCommand.add("java");
        discoveryCommand.add("-jar");
        discoveryCommand.add("../eureka-discovery/build/libs/eureka-discovery.jar");

        ProcessBuilder builder1 = new ProcessBuilder(discoveryCommand);
        discoveryProcess = builder1.inheritIO().start();
    }


}
