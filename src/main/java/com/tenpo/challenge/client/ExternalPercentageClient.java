package com.tenpo.challenge.client;

import com.tenpo.challenge.exception.ExternalPercentageException;
import com.tenpo.challenge.model.PercentageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class ExternalPercentageClient {
    private static final Logger logger = LoggerFactory.getLogger(ExternalPercentageClient.class);
    private final WebClient webClient;

    public ExternalPercentageClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:1080").build();
    }

    public Mono<PercentageEntity> getPercentage(){
        return webClient.get().uri("/percentage")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        Mono.error(new ExternalPercentageException("4xx error occurred while making the request")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        Mono.error(new ExternalPercentageException("5xx error occurred while making the request")))
                .bodyToMono(Double.class)
                .doFirst(() -> logger.info("Calling to external percentage service."))
                .retry(3)
                .map(result -> new PercentageEntity(result, Instant.now()))
                .doOnNext(response -> logger.info("Received response from external percentage service: {}", response.getPercentage()));
    }
}
