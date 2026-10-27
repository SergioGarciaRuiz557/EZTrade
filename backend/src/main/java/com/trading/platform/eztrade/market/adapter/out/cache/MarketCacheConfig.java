package com.trading.platform.eztrade.market.adapter.out.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Infrastructure configuration for caching market provider responses.
 */
@Configuration
@EnableCaching
public class MarketCacheConfig {

    @Bean
    public CacheManager cacheManager(@Value("${market.cache.ttl-seconds:30}") long ttlSeconds) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(List.of("marketPrice", "instrumentOverview", "dailyCandles"));
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
                .maximumSize(1_000));
        return cacheManager;
    }
}
