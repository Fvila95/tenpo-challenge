package com.tenpo.challenge.aspect;

import com.tenpo.challenge.exception.TooManyRequestsException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

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

        String redisKey = "RPM:" + getClientIp();

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
                        return Mono.fromCallable(() -> {
                            try {
                                return joinPoint.proceed();
                            } catch (Throwable e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } else {
                        logger.warn("RPM limit exceeded for {}.{}", controllerName, methodName);
                        return Mono.error(new TooManyRequestsException("Too many requests. Please try again later."));
                    }
                });
    }

    private String getClientIp() {
        return "127.0.0.1";
    }

}
