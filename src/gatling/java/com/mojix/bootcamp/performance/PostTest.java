package com.mojix.bootcamp.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.time.Duration;

public class PostTest extends Simulation {

    public static final String CONCURRENT_USERS = "1";

    public static String BASE_URL = "https://api.restful-api.dev/objects";
    HttpProtocolBuilder httpProtocol = http.baseUrl( BASE_URL );

    String payload = """
        {
           "name": "Apple MacBook Pro 16",
           "data": {
              "year": 2019,
              "price": 1849.99,
              "CPU model": "Intel Core i9",
              "Hard disk size": "1 TB"
           }
        }
    """;

    ScenarioBuilder scenario = scenario("Create a Phone")
        .exec(
            http("Create & Validate")
            .post( BASE_URL )
            .body( StringBody(payload) )
            .asJson()
            .check(jmesPath("id").find().saveAs("newId"))
            .check(jmesPath("createdAt").find().saveAs("newCreatedAt"))
            .check(status().is(200))
        )
        .exec(
                session -> {
                    System.out.println("Created Id: " + session.getString("newId"));
                    System.out.println("Created At: " + session.getString("newCreatedAt"));

                    http("Check the phone created")
                            .get("/" + session.getString("newId"))
                            .check(jmesPath("id").notNull())
                            .check(jmesPath("name").notNull())
                            .check(jmesPath("name").find().saveAs("respName"))
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
