package org.kiteseven.kiteuniverse.support.cache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.kiteseven.kiteuniverse.config.properties.TwoLevelCacheProperties;
import org.kiteseven.kiteuniverse.support.redis.RedisKeyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Default two-level cache implementation backed by Caffeine and Redis.
 */
@Service
@ConditionalOnProperty(prefix = "kite-universe.cache.two-level", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisCaffeineTwoLevelCacheService implements TwoLevelCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCaffeineTwoLevelCacheService.class);

    private final Cache<String, LocalCacheEntry> localCache;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisKeyManager redisKeyManager;
    private final String invalidationTopic;

    public RedisCaffeineTwoLevelCacheService(StringRedisTemplate stringRedisTemplate,
                                             ObjectMapper objectMapper,
                                             RedisKeyManager redisKeyManager,
                                             TwoLevelCacheProperties twoLevelCacheProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.redisKeyManager = redisKeyManager;
        this.localCache = Caffeine.newBuilder()
                .maximumSize(Math.max(twoLevelCacheProperties.getLocalMaximumSize(), 1L))
                .build();
        this.invalidationTopic = redisKeyManager.buildKey(twoLevelCacheProperties.getInvalidationTopic());
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Object get(String keySuffix, JavaType javaType, Duration localTtl) {
        String fullCacheKey = resolveFullCacheKey(keySuffix);
        Object localValue = getLocal(fullCacheKey);
        if (localValue != null) {
            return localValue;
        }

        try {
            String cachedValue = stringRedisTemplate.opsForValue().get(fullCacheKey);
            if (!StringUtils.hasText(cachedValue)) {
                return null;
            }
            Object deserializedValue = objectMapper.readValue(cachedValue, javaType);
            putLocal(fullCacheKey, deserializedValue, localTtl);
            return deserializedValue;
        } catch (Exception exception) {
            log.warn("Two-level cache read skipped for key {}", fullCacheKey, exception);
            return null;
        }
    }

    @Override
    public void put(String keySuffix, Object value, Duration localTtl, Duration redisTtl) {
        String fullCacheKey = resolveFullCacheKey(keySuffix);
        putLocal(fullCacheKey, value, localTtl);
        try {
            stringRedisTemplate.opsForValue().set(
                    fullCacheKey,
                    objectMapper.writeValueAsString(value),
                    normalizeDuration(redisTtl)
            );
        } catch (Exception exception) {
            log.warn("Two-level cache write skipped for key {}", fullCacheKey, exception);
        }
    }

    @Override
    public void evict(String keySuffix) {
        String fullCacheKey = resolveFullCacheKey(keySuffix);
        localCache.invalidate(fullCacheKey);

        try {
            stringRedisTemplate.delete(fullCacheKey);
        } catch (Exception exception) {
            log.warn("Two-level Redis eviction skipped for key {}", fullCacheKey, exception);
        }

        try {
            stringRedisTemplate.convertAndSend(invalidationTopic, fullCacheKey);
        } catch (Exception exception) {
            log.warn("Two-level invalidation publish skipped for key {}", fullCacheKey, exception);
        }
    }

    @Override
    public void evictLocalByFullKey(String fullCacheKey) {
        if (!StringUtils.hasText(fullCacheKey)) {
            return;
        }
        localCache.invalidate(fullCacheKey.trim());
    }

    private Object getLocal(String fullCacheKey) {
        LocalCacheEntry entry = localCache.getIfPresent(fullCacheKey);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            localCache.invalidate(fullCacheKey);
            return null;
        }
        return entry.value();
    }

    private void putLocal(String fullCacheKey, Object value, Duration localTtl) {
        localCache.put(fullCacheKey, new LocalCacheEntry(
                value,
                System.currentTimeMillis() + normalizeDuration(localTtl).toMillis()
        ));
    }

    private String resolveFullCacheKey(String keySuffix) {
        return redisKeyManager.buildKey(keySuffix);
    }

    private Duration normalizeDuration(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return Duration.ofSeconds(1L);
        }
        return duration;
    }

    /**
     * Holds one local Caffeine value plus its expiration timestamp.
     *
     * @param value cached value
     * @param expireAtMillis absolute expiration time in milliseconds
     */
    private record LocalCacheEntry(Object value, long expireAtMillis) {

        private boolean isExpired() {
            return expireAtMillis <= System.currentTimeMillis();
        }
    }
}
