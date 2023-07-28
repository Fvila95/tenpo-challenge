package com.tenpo.challenge.configuration;

import jakarta.annotation.PostConstruct;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockServerConfig {

    @Value("${mockserver.port}")
    private int port;

    @PostConstruct
    public void setupMockServer() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MockServerClient mockServer = new MockServerClient("localhost", port);
        mockServer.when(HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/percentage"))
                .respond(HttpResponse.response()
                        .withBody("10", MediaType.APPLICATION_JSON));
    }
}