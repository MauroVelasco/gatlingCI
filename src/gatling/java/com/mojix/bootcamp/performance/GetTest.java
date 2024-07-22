package com.mojix.bootcamp.performance;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class GetTest extends Simulation {

    public static final String CONCURRENT_USERS = "1";

    HttpProtocolBuilder httpProtocol = http.baseUrl( "https://api.restful-api.dev/objects" );

    FeederBuilder.FileBased<Object> feederIds = jsonFile("data/devicesId.json").circular();

    ScenarioBuilder scenario = scenario("List Phones By Id")
        .feed( feederIds )
        .exec(
            http("GET Phone Name")
            .get("/#{id}")
            .check(jmesPath("name").find().saveAs("name"))
            .check(status().is(200))
        )
        .exec(
            session -> {
                return session;
            }
        );

    {
        setUp(
            scenario.injectClosed(
                    constantConcurrentUsers(Integer.parseInt(CONCURRENT_USERS)).during(Duration.ofSeconds(5)
                )
            )
        ).protocols(httpProtocol);
    }

}
