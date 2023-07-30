package com.tenpo.challenge.service;

import com.tenpo.challenge.client.ExternalPercentageClient;
import com.tenpo.challenge.dto.PercentageCalculationDTO;
import com.tenpo.challenge.dto.RequestNumbersDTO;
import com.tenpo.challenge.exception.ExternalPercentageException;
import com.tenpo.challenge.exception.PercentageCalculationException;
import com.tenpo.challenge.model.PercentageCalculation;
import com.tenpo.challenge.repository.PercentageCalculationRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;

@Service
public class PercentageCalculatorService {

    public static final String PERCENTAGE_KEY = "percentage";
    private final ReactiveRedisTemplate<String, Double> redisTemplate;
    private final PercentageCalculationRepository percentageCalculationRepository;
    private final ExternalPercentageClient externalPercentageClient;
    private static final Logger logger = LoggerFactory.getLogger(ExternalPercentageClient.class);
    private final Integer cacheDuration;
    private final ModelMapper mapper;


    @Autowired
    public PercentageCalculatorService(ReactiveRedisTemplate<String, Double> redisTemplate, PercentageCalculationRepository percentageCalculationRepository, ExternalPercentageClient externalPercentageClient, @Value("${cache.percentage.duration}") Integer cacheDuration) {
        this.redisTemplate = redisTemplate;
        this.percentageCalculationRepository = percentageCalculationRepository;
        this.externalPercentageClient = externalPercentageClient;
        this.cacheDuration = cacheDuration;
        this.mapper = new ModelMapper();
    }

    public Mono<Double> calculatePercentage(RequestNumbersDTO requestNumbersDTO) {
        return redisTemplate.opsForValue().get(PERCENTAGE_KEY)
                .doFirst(() -> logger.info("Searching percentage from cache."))
                .switchIfEmpty(retrieveAndCachePercentage())
                .map(percentage -> performCalculation(requestNumbersDTO, percentage))
                .doOnNext(percentageCalculation -> savePercentageCalculation(percentageCalculation)
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe(
                                result -> logger.info("Successfully saved percentage calculation {}", result),
                                throwable -> logger.error("An error occurred when saving percentage calculation: {}", throwable.getMessage())
                        )
                )
                .map(PercentageCalculation::getResult);
    }

    private Mono<Double> retrieveAndCachePercentage() {
        return externalPercentageClient.getPercentage()
                .doFirst(() -> logger.info("Percentage isn't founded in the cache."))
                .onErrorResume(exception -> {
                    logger.error("An error occurred while calling the External Percentage Service: {}", exception);
                    return Mono.just(percentageCalculationRepository.findFirstByOrderByIdDesc())
                            .map(PercentageCalculation::getPercentage)
                            .onErrorMap(throwable -> new PercentageCalculationException(throwable.getMessage()));
                })
                .flatMap(this::storePercentageInCache);
    }

    private Mono<PercentageCalculation> savePercentageCalculation(PercentageCalculation percentageCalculation) {
        return Mono.fromCallable(() -> percentageCalculationRepository.save(percentageCalculation))
                .doFirst(() -> logger.info("Saving percentage calculation {}", percentageCalculation))
                .thenReturn(percentageCalculation)
                .onErrorResume(throwable -> Mono.just(percentageCalculation))
                .doOnError(throwable -> logger.error("An error occurred when saving percentage calculation {} : {}", percentageCalculation, throwable.getMessage()));
    }

    private Mono<Double> storePercentageInCache(Double percentage) {
        return redisTemplate.opsForValue().set(PERCENTAGE_KEY, percentage, Duration.ofMinutes(cacheDuration))
                .doFirst(() -> logger.info("Storing into cache the following percentage {} with 30 minutes of duration.", percentage))
                .thenReturn(percentage)
                .doOnError(throwable -> logger.error("An error occurred when storing percentage into cache: {} ", throwable.getMessage()))
                .onErrorResume(throwable -> Mono.just(percentage));
    }

    private PercentageCalculation performCalculation(RequestNumbersDTO requestNumbersDTO, Double percentage) {
        Double sum = requestNumbersDTO.getFirstNumber() + requestNumbersDTO.getSecondNumber();
        Double result = sum + sum * (percentage / 100);
        return new PercentageCalculation(percentage, requestNumbersDTO.getFirstNumber(), requestNumbersDTO.getSecondNumber(), result, Instant.now());
    }

    public Mono<Page<PercentageCalculationDTO>> findAllCalculations(int page, int size) {
        return Mono.just(percentageCalculationRepository.findAll(PageRequest.of(page, size)))
                .map(percentageCalculations -> percentageCalculations.map(this::toDTO));
    }

    public PercentageCalculationDTO toDTO(PercentageCalculation entity) {
        return mapper.map(entity, PercentageCalculationDTO.class);
    }}
