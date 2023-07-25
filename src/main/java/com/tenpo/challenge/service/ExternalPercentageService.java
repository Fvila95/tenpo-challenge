package com.tenpo.challenge.service;

import com.tenpo.challenge.model.PercentageEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Service
public class ExternalPercentageService {

    private final WebClient webClient;
    private final ReactiveRedisTemplate<String, Double> redisTemplate;
    Perc

    @Autowired
    public ExternalPercentageService(WebClient.Builder webClientBuilder, ReactiveRedisTemplate<String, Double> redisTemplate) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:1080").build();
        this.redisTemplate = redisTemplate;
    }

    public Mono<Double> getPercentage() {
        return redisTemplate.opsForValue().get("percentage")
                .switchIfEmpty(webClient.get().uri("/percentage")
                        .retrieve()
                        .bodyToMono(Double.class)
                        .retry(3)
                        .map(result -> new PercentageEntity(result, Instant.now()))
                        .flatMap(percentageEntity -> )
                        .flatMap(result -> redisTemplate.opsForValue().set("percentage", result, Duration.ofMinutes(30)).thenReturn(result)));
    }
}
