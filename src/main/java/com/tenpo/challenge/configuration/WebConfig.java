package com.tenpo.challenge.configuration;

import com.tenpo.challenge.filter.RateLimitingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebFilter;

import java.util.List;

@Configuration
public class WebConfig implements WebFluxConfigurer {
    @Bean
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions
                .route(RequestPredicates.all(), request -> ServerResponse.ok().build());
    }

    @Bean
    public WebFilter rateLimitingFilter(ReactiveStringRedisTemplate redisTemplate) {
        return new RateLimitingFilter(redisTemplate);
    }
}
