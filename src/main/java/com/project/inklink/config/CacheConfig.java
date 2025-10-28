package com.project.inklink.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Profile("dev")
    public CacheManager devCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of("stories", "users", "categories", "trending", "search"));
        return cacheManager;
    }

    // For production, you would configure Redis here
    /*
    @Bean
    @Profile("prod")
    public CacheManager redisCacheManager() {
        RedisCacheManager redisCacheManager = RedisCacheManager.create(redisConnectionFactory);
        redisCacheManager.setCacheDefaults(cacheConfiguration);
        return redisCacheManager;
    }
    */
}