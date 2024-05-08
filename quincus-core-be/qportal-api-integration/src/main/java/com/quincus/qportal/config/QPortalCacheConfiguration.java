package com.quincus.qportal.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ConditionalOnProperty(name = "cache.enabled", havingValue = "true", prefix = "management")
@Configuration
@EnableCaching
public class QPortalCacheConfiguration {

    @Value("${cache.expireAfterWriteMinutes}")
    private int expireAfterWriteMinutes;

    @Value("${cache.maximumSize}")
    private int maximumSize;

    @Value("${cache.cacheNames}")
    private List<String> cacheNames;

    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeineCacheBuilder());
        cacheManager.setCacheNames(cacheNames);
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(expireAfterWriteMinutes, TimeUnit.MINUTES)
                .maximumSize(maximumSize)
                .recordStats();
    }

}
