package com.mojix.bootcamp.performance;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class PutTest extends Simulation {

    public static final String CONCURRENT_USERS = "1";

    public static String BASE_URL = "https://api.restful-api.dev/objects/ff80818190afe51a0190b39394b20435";
    HttpProtocolBuilder httpProtocol = http.baseUrl( BASE_URL );

    String payload = """
        {
           "name": "Apple MacBook Pro 16",
           "data": {
              "year": 2019,
              "price": 2049.99,
              "CPU model": "Intel Core i9",
              "Hard disk size": "1 TB",
              "color": "silver"
           }
        }
    """;

    ScenarioBuilder scenario = scenario("Update a Phone")
        .exec(
            http("Update & Validate")
            .put( BASE_URL )
            .body( StringBody(payload) )
            .asJson()
            .check(jmesPath("id").find().saveAs("updatedId"))
            .check(status().is(200))
        )
        .exec(
                session -> {
                    System.out.println("Updated Id: " + session.getString("updatedId"));

                    http("Check the phone updated")
                            .get("/" + session.getString("updatedId"))
                            .check(jmesPath("id").notNull())
                            .check(jmesPath("name").notNull())
                            .check(bodyString().saveAs("BODY"))
                            .check(status().is(200));

                    return session;
                }
        );

    {
        setUp(
            scenario.injectClosed(
                    constantConcurrentUsers(Integer.parseInt(CONCURRENT_USERS)).during(Duration.ofSeconds(1)
                )
            )
        ).protocols(httpProtocol);
    }

}
