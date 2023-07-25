package com.tenpo.challenge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Random;

@Service
public class ExternalService {

    private final WebClient webClient;

    @Autowired
    public ExternalService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:1080").build();
    }

    public Mono<Double> getPercentage() {
        return webClient.get().uri("/percentage")
                .retrieve()
                .bodyToMono(Double.class);
    }
}
