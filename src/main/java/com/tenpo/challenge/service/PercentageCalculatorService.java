package com.tenpo.challenge.service;

import com.tenpo.challenge.client.ExternalPercentageClient;
import com.tenpo.challenge.exception.ExternalPercentageException;
import com.tenpo.challenge.exception.PercentageEntityException;
import com.tenpo.challenge.model.PercentageCalculation;
import com.tenpo.challenge.repository.PercentageCalculationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;

@Service
public class PercentageCalculatorService {

    private final ReactiveRedisTemplate<String, Double> redisTemplate;
    private final PercentageCalculationRepository percentageCalculationRepository;
    private final ExternalPercentageClient externalPercentageClient;
    private static final Logger logger = LoggerFactory.getLogger(ExternalPercentageClient.class);
    private final Integer cacheDuration;


    @Autowired
    public PercentageCalculatorService(ReactiveRedisTemplate<String, Double> redisTemplate, PercentageCalculationRepository percentageCalculationRepository, ExternalPercentageClient externalPercentageClient, @Value("${cache.percentage.duration}") Integer cacheDuration) {
        this.redisTemplate = redisTemplate;
        this.percentageCalculationRepository = percentageCalculationRepository;
        this.externalPercentageClient = externalPercentageClient;
        this.cacheDuration = cacheDuration;
    }

    public Mono<Double> calculatePercentage(Double firstNumber, Double secondNumber) {
        return redisTemplate.opsForValue().get("percentage")
                .doFirst(() -> logger.info("Searching percentage from cache."))
                .switchIfEmpty(externalPercentageClient.getPercentage()
                        .doFirst(() -> logger.info("Percentage isn't founded in the cache."))
                        .onErrorResume(ExternalPercentageException.class, e -> {
                            logger.error("An error occurred while calling the External Percentage Service: {}", e);
                            return Mono.just(percentageCalculationRepository.findFirstByOrderByIdDesc())
                                    .map(PercentageCalculation::getPercentage)
                                    .onErrorMap(throwable -> new PercentageEntityException(throwable.getMessage()));
                        })
                        .map(percentage -> performCalculation(firstNumber, secondNumber, percentage))
                        .flatMap(this::savePercentageCalculation)
                        .flatMap(this::storePercentageInCache));
    }

    private Mono<PercentageCalculation> savePercentageCalculation(PercentageCalculation percentageCalculation) {
        return Mono.fromCallable(() -> percentageCalculationRepository.save(percentageCalculation))
                .doFirst(() -> logger.info("Saving percentage calculation {}", percentageCalculation))
                .subscribeOn(Schedulers.boundedElastic())
                .thenReturn(percentageCalculation)
                .onErrorResume(throwable -> Mono.just(percentageCalculation))
                .doOnError(throwable -> logger.error("An error ocurred when saving percentage calculation {} : {}", percentageCalculation, throwable.getMessage()));
    }

    private Mono<Double> storePercentageInCache(PercentageCalculation percentageCalculation) {
        return redisTemplate.opsForValue().set("percentage", percentageCalculation.getPercentage(), Duration.ofMinutes(cacheDuration))
                .doFirst(() -> logger.info("Storing into cache the following percentage {} with 30 minutes of duration.", percentageCalculation.getPercentage()))
                .thenReturn(percentageCalculation.getPercentage())
                .doOnError(throwable -> logger.error("An error ocurred when storing percentage into cache: {} ", throwable.getMessage()));
    }

    private PercentageCalculation performCalculation(Double firstNumber, Double secondNumber, Double percentage){
        Double sum = firstNumber + secondNumber;
        Double result = sum + sum * (percentage / 100);
        return new PercentageCalculation(percentage, firstNumber, secondNumber, result, Instant.now());
    }


    public Page<PercentageCalculation> findAllCalculations(int page, int size) {
        return percentageCalculationRepository.findAll(PageRequest.of(page, size));
    }
}
