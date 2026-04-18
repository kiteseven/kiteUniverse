package org.kiteseven.kiteuniverse.support.cache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiteseven.kiteuniverse.config.properties.TwoLevelCacheProperties;
import org.kiteseven.kiteuniverse.support.redis.RedisKeyManager;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisCaffeineTwoLevelCacheServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RedisKeyManager redisKeyManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RedisCaffeineTwoLevelCacheService cacheService;

    private JavaType mapType;

    @BeforeEach
    void setUp() {
        TwoLevelCacheProperties properties = new TwoLevelCacheProperties();
        properties.setLocalMaximumSize(100L);
        properties.setInvalidationTopic("cache:two-level:invalidate");

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisKeyManager.buildKey("cache:two-level:invalidate"))
                .thenReturn("kite-universe:cache:two-level:invalidate");
        lenient().when(redisKeyManager.buildKey("content:home")).thenReturn("kite-universe:content:home");
        lenient().when(redisKeyManager.buildKey("content:boards")).thenReturn("kite-universe:content:boards");

        cacheService = new RedisCaffeineTwoLevelCacheService(
                stringRedisTemplate,
                objectMapper,
                redisKeyManager,
                properties
        );
        mapType = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
    }

    @Test
    void getShouldReadRedisOnceAndHydrateLocalCache() throws Exception {
        Map<String, Object> payload = Map.of("title", "home");
        when(valueOperations.get("kite-universe:content:home"))
                .thenReturn(objectMapper.writeValueAsString(payload));

        Object firstLoad = cacheService.get("content:home", mapType, Duration.ofSeconds(30));
        Object secondLoad = cacheService.get("content:home", mapType, Duration.ofSeconds(30));

        assertEquals(payload, firstLoad);
        assertEquals(payload, secondLoad);
        verify(valueOperations, times(1)).get("kite-universe:content:home");
    }

    @Test
    void putShouldPopulateLocalCacheAndAvoidRedisReadOnHotKey() {
        Map<String, Object> payload = Map.of("title", "boards");

        cacheService.put("content:boards", payload, Duration.ofSeconds(30), Duration.ofSeconds(300));
        Object cached = cacheService.get("content:boards", mapType, Duration.ofSeconds(30));

        assertEquals(payload, cached);
        verify(valueOperations, never()).get("kite-universe:content:boards");
        verify(valueOperations).set(
                eq("kite-universe:content:boards"),
                any(String.class),
                eq(Duration.ofSeconds(300))
        );
    }

    @Test
    void evictShouldDeleteRedisPublishInvalidationAndClearLocalCache() {
        Map<String, Object> payload = Map.of("title", "home");
        cacheService.put("content:home", payload, Duration.ofSeconds(30), Duration.ofSeconds(300));

        cacheService.evict("content:home");
        Object cached = cacheService.get("content:home", mapType, Duration.ofSeconds(30));

        assertNull(cached);
        verify(stringRedisTemplate).delete("kite-universe:content:home");
        verify(stringRedisTemplate).convertAndSend(
                "kite-universe:cache:two-level:invalidate",
                "kite-universe:content:home"
        );
        verify(valueOperations, times(1)).get("kite-universe:content:home");
    }

    @Test
    void evictLocalByFullKeyShouldOnlyClearTheLocalEntry() throws Exception {
        Map<String, Object> payload = Map.of("title", "boards");
        when(valueOperations.get("kite-universe:content:boards"))
                .thenReturn(objectMapper.writeValueAsString(payload));

        cacheService.put("content:boards", payload, Duration.ofSeconds(30), Duration.ofSeconds(300));
        cacheService.evictLocalByFullKey("kite-universe:content:boards");
        Object cached = cacheService.get("content:boards", mapType, Duration.ofSeconds(30));

        assertEquals(payload, cached);
        verify(valueOperations, times(1)).get("kite-universe:content:boards");
        verify(stringRedisTemplate, never()).delete(any(String.class));
        verify(stringRedisTemplate, never()).convertAndSend(any(String.class), any(String.class));
    }
}
