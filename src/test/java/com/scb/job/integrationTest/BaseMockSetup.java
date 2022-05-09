package com.scb.job.integrationTest;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Parameter;

public class BaseMockSetup {

  public static void setUp(ClientAndServer mockServer) throws IOException {

    mockServer
        .when(
            request()
                .withMethod("POST")
                .withPath("/pricing/estimate-price")
        )
        .respond(
            response()
                .withBody("{\n"
                    + "    \"normalPrice\": 210.0,\n"
                    + "    \"netPrice\": 210.0,\n"
                    + "    \"discount\": 0.0,\n"
                    + "    \"distance\": 8.0,\n"
                    + "    \"tripDuration\": 0\n"
                    + "}")
                .withStatusCode(200)
                .withHeader("Content-Type", "application/json")
        );

    Map<String, List<String>> parameters = new HashMap<>();
    parameters.put("location", Arrays.asList("1.2,1.1"));

    mockServer
        .when(
            request()
                .withMethod("GET")
                .withPath("/api/zone/")
                .withQueryStringParameters(parameters)
        )
        .respond(
            response()
                .withBody("{\n"
                    + "    \"zoneId\": 1,\n"
                    + "    \"postalCode\": 111,\n"
                    + "    \"zoneName\": \"Khlong Toei\"\n"
                    + "}")
                .withStatusCode(200)
                .withHeader("Content-Type", "application/json")
        );

      mockServer
              .when(
                      request()
                              .withMethod("GET")
                              .withPath("/api/address")
                              .withQueryStringParameter(Parameter.param("longitude", "1.2"))
                              .withQueryStringParameter(Parameter.param("latitude", "1.1"))
              )
              .respond(
                      response()
                              .withBody("{\n"
                                      + "    \"subDistrict\": \"sub district\""
                                      + "}")
                              .withStatusCode(200)
                              .withHeader("Content-Type", "application/json")
              );

  }
}