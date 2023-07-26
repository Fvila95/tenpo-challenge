package com.tenpo.challenge.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.*;

@Configuration
public class RedisConfiguration {

    @Bean
    public ReactiveRedisTemplate<String, Double> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericToStringSerializer<Double> valueSerializer = new GenericToStringSerializer<>(Double.class);

        RedisSerializationContext<String, Double> context =
                RedisSerializationContext.<String, Double>newSerializationContext(keySerializer)
                        .value(valueSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

}
