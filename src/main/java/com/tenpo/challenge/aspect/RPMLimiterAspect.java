package com.tenpo.challenge.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Aspect
@Component
public class RPMLimiterAspect {

    private static final Logger logger = LoggerFactory.getLogger(RPMLimiterAspect.class);
    private static final int MAX_REQUESTS_PER_MINUTE = 3;
    private static final long RPM_TTL_SECONDS = 60;

    private final ReactiveRedisTemplate<String, Double> reactiveRedisTemplate;

    @Autowired
    public RPMLimiterAspect(ReactiveRedisTemplate<String, Double> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    @Around("execution(* com.tenpo.challenge.controller.ApiController.*(..))")
    public Mono<Object> limitRpm(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String controllerName = joinPoint.getTarget().getClass().getSimpleName();

        String redisKey = "RPM";

        return reactiveRedisTemplate.opsForValue().increment(redisKey, 1.0)
                .flatMap(count -> {
                    if (count == 1.0) {
                        // Set TTL for the key if it's the first request
                        return reactiveRedisTemplate.expire(redisKey, Duration.ofSeconds(RPM_TTL_SECONDS))
                                .thenReturn(count);
                    } else {
                        return Mono.just(count);
                    }
                })
                .flatMap(count -> {
                    if (count <= MAX_REQUESTS_PER_MINUTE) {
                        logger.info("RPM limit not exceeded. Proceeding with method: {}.{}", controllerName, methodName);
                        try {
                            return Mono.just(joinPoint.proceed());
                        } catch (TooManyRequestsException e) {
                            logger.warn("TooManyRequestsException handled for {}.{}", controllerName, methodName);
                            return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests. Please try again later."));
                        } catch (Throwable e) {
                            return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests. Please try again later."));
                        }
                    } else {
                        logger.warn("RPM limit exceeded for {}.{}", controllerName, methodName);
                        return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests. Please try again later."));
                    }
                }).onErrorResume(TooManyRequestsException.class, ex -> {
                    logger.warn("TooManyRequestsException handled for {}.{}", controllerName, methodName);
                    return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage()));
                });
    }
    public static class TooManyRequestsException extends RuntimeException {
        public TooManyRequestsException(String message) {
            super(message);
        }
    }
}
