package com.tenpo.challenge.service;

import com.tenpo.challenge.model.PercentageEntity;
import com.tenpo.challenge.repository.PercentageRepository;
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
    private final PercentageRepository percentageRepository;

    @Autowired
    public ExternalPercentageService(WebClient.Builder webClientBuilder, ReactiveRedisTemplate<String, Double> redisTemplate, PercentageRepository percentageRepository) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:1080").build();
        this.redisTemplate = redisTemplate;
        this.percentageRepository=percentageRepository;
    }

    public Mono<Double> getPercentage() {
        return redisTemplate.opsForValue().get("percentage")
                .switchIfEmpty(webClient.get().uri("/percentage")
                        .retrieve()
                        .bodyToMono(Double.class)
                        .retry(3)
                        .map(result -> new PercentageEntity(result, Instant.now()))
                        .map(percentageEntity -> percentageRepository.save(percentageEntity) )
                        .flatMap(percentageEntity -> redisTemplate.opsForValue().set("percentage", percentageEntity.getPercentage(), Duration.ofMinutes(30)).thenReturn(percentageEntity.getPercentage())));
    }
}
