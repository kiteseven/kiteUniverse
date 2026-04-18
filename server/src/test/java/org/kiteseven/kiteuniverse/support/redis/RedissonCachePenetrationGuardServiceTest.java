package org.kiteseven.kiteuniverse.support.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiteseven.kiteuniverse.config.properties.RedisBloomFilterProperties;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedissonCachePenetrationGuardServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RedisKeyManager redisKeyManager;

    @Mock
    private RBloomFilter<Long> userBloomFilter;

    @Mock
    private RBloomFilter<Long> postBloomFilter;

    private RedissonCachePenetrationGuardService guardService;

    @BeforeEach
    void setUp() {
        RedisBloomFilterProperties properties = new RedisBloomFilterProperties();

        when(redisKeyManager.buildKey("bloom-filter:user-ids")).thenReturn("kite-universe:bloom-filter:user-ids");
        when(redisKeyManager.buildKey("bloom-filter:post-ids")).thenReturn("kite-universe:bloom-filter:post-ids");
        when(redissonClient.<Long>getBloomFilter("kite-universe:bloom-filter:user-ids")).thenReturn(userBloomFilter);
        when(redissonClient.<Long>getBloomFilter("kite-universe:bloom-filter:post-ids")).thenReturn(postBloomFilter);

        guardService = new RedissonCachePenetrationGuardService(redissonClient, properties, redisKeyManager);
        guardService.prepareFilters();
    }

    @Test
    void addUserIdsShouldInitializeAndPopulateBloomFilter() {
        guardService.addUserIds(List.of(1L, 2L));

        verify(userBloomFilter).tryInit(200000L, 0.001D);
        verify(userBloomFilter).add(1L);
        verify(userBloomFilter).add(2L);
    }

    @Test
    void mightContainUserIdShouldFailOpenWhenInitializationFails() {
        when(userBloomFilter.tryInit(200000L, 0.001D)).thenThrow(new RuntimeException("redis down"));

        assertTrue(guardService.mightContainUserId(9L));
    }
}
