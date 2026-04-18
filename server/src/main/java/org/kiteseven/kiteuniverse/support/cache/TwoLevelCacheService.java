package org.kiteseven.kiteuniverse.support.cache;

import com.fasterxml.jackson.databind.JavaType;

import java.time.Duration;

/**
 * Provides Caffeine + Redis two-level cache operations.
 */
public interface TwoLevelCacheService {

    /**
     * @return whether the infrastructure is enabled
     */
    boolean isEnabled();

    /**
     * Reads a cached value using the configured two-level lookup order.
     *
     * @param keySuffix business cache key suffix
     * @param javaType target return type
     * @param localTtl local cache TTL
     * @return cached value or null
     */
    Object get(String keySuffix, JavaType javaType, Duration localTtl);

    /**
     * Stores a value into both cache levels.
     *
     * @param keySuffix business cache key suffix
     * @param value cached value
     * @param localTtl local cache TTL
     * @param redisTtl redis TTL
     */
    void put(String keySuffix, Object value, Duration localTtl, Duration redisTtl);

    /**
     * Evicts the key from Redis and all local caches.
     *
     * @param keySuffix business cache key suffix
     */
    void evict(String keySuffix);

    /**
     * Evicts the key from the local cache using the fully prefixed Redis key.
     *
     * @param fullCacheKey full Redis cache key
     */
    void evictLocalByFullKey(String fullCacheKey);
}
