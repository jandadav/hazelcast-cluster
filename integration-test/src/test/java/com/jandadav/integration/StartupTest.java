package com.jandadav.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class StartupTest {

    @Test
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
}
