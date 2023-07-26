package com.tenpo.challenge.service;

import com.tenpo.challenge.client.ExternalPercentageClient;
import com.tenpo.challenge.exception.ExternalPercentageException;
import com.tenpo.challenge.exception.PercentageEntityException;
import com.tenpo.challenge.model.PercentageEntity;
import com.tenpo.challenge.repository.PercentageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Service
public class PercentageCalculatorService {

    private final ReactiveRedisTemplate<String, Double> redisTemplate;
    private final PercentageRepository percentageRepository;
    private final ExternalPercentageClient externalPercentageClient;
    private static final Logger logger = LoggerFactory.getLogger(ExternalPercentageClient.class);


    @Autowired
    public PercentageCalculatorService(ReactiveRedisTemplate<String, Double> redisTemplate, PercentageRepository percentageRepository, ExternalPercentageClient externalPercentageClient) {
        this.redisTemplate = redisTemplate;
        this.percentageRepository=percentageRepository;
        this.externalPercentageClient = externalPercentageClient;
    }

    public Mono<Double> calculatePercentage() {
        return redisTemplate.opsForValue().get("percentage")
                .switchIfEmpty(externalPercentageClient.getPercentage()
                        .flatMap(percentageEntity -> Mono.fromCallable(() -> percentageRepository.save(percentageEntity))
                                .subscribeOn(Schedulers.boundedElastic())
                                .thenReturn(percentageEntity))
                        .onErrorResume(ExternalPercentageException.class, e -> {
                            logger.error("An error occurred while calling the External Percentage Service: {}", e);
                            return Mono.just(percentageRepository.findFirstByOrderByIdDesc())
                                    .onErrorMap(throwable -> new PercentageEntityException(throwable.getMessage()));
                        })
                        .flatMap(this::storePercentageInCache));
    }

    private Mono<Double> storePercentageInCache(PercentageEntity percentageEntity) {
        return redisTemplate.opsForValue().set("percentage", percentageEntity.getPercentage(), Duration.ofMinutes(30)).thenReturn(percentageEntity.getPercentage());
    }
}
