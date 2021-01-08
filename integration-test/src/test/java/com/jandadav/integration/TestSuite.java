package com.jandadav.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestSuite {

    @Test
    @Order(1)
    void clusterIsUp() {
        given()
                .accept(ContentType.JSON)
        .when()
                .get("http://localhost:8761/eureka/apps")
        .then()
                .statusCode(200)
                .body( containsStringIgnoringCase("hazelcast-node:9090"))
                .body( containsStringIgnoringCase("hazelcast-node:9091"));
    }


    @Test
    @Order(2)
    void dataReplicateThroughHazelcast() {
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
        given()
                .formParam("key", "key1")
                .when()
                .get("http://localhost:9091/get")
                .then()
                .statusCode(200)
                .body(containsStringIgnoringCase("value1"));

        // such speed
    }

}
