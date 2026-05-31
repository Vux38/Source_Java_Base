package com.vux38.base.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

@Configuration
public class RedisCacheTtlConfig {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(
            @Value("${app.cache.default-ttl-seconds:300}") long ttl) {

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(ttl))
                .disableCachingNullValues();
    }
}