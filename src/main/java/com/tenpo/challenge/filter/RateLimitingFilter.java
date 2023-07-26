package com.tenpo.challenge.filter;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class RateLimitingFilter implements WebFilter {

    private static final String PREFIX = "ratelimiter:";
    private static final int LIMIT = 3;
    private final ReactiveValueOperations<String, String> valueOperations;

    public RateLimitingFilter(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.valueOperations = redisTemplate.opsForValue();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String id = exchange.getRequest().getRemoteAddress().toString(); // get the id of the user (IP in this case)
        String key = PREFIX + id;

        return valueOperations.get(key)
                .defaultIfEmpty("0")
                .flatMap(currentVal -> {
                    int currentValInt = Integer.parseInt(currentVal);
                    if (currentValInt < LIMIT) {
                        return valueOperations.set(key, String.valueOf(currentValInt + 1), Duration.ofMinutes(1))
                                .then(chain.filter(exchange));
                    } else {
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        return exchange.getResponse().setComplete();
                    }
                });
    }
}
