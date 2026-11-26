package com.learnsphere.resourceservice.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

// Enables Spring's caching support.
// Caffeine, the cache name and the TTL are configured in application.properties.
@Configuration
@EnableCaching
public class CacheConfig {
}
