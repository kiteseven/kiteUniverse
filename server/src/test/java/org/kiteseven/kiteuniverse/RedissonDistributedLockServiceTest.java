package org.kiteseven.kiteuniverse;

import org.junit.jupiter.api.Test;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.config.properties.RedisLockProperties;
import org.kiteseven.kiteuniverse.support.redis.RedisKeyManager;
import org.kiteseven.kiteuniverse.support.redis.RedissonDistributedLockService;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedissonDistributedLockServiceTest {

    @Test
    void executeWithLockShouldRunActionAndReleaseLock() throws Exception {
        RedissonClient redissonClient = Mockito.mock(RedissonClient.class);
        RedisKeyManager redisKeyManager = Mockito.mock(RedisKeyManager.class);
        RLock lock = Mockito.mock(RLock.class);

        RedisLockProperties properties = new RedisLockProperties();
        properties.setKeyPrefix("lock");
        properties.setWaitTimeMillis(1500L);
        properties.setLeaseTimeMillis(5000L);

        when(redisKeyManager.buildKey("lock:community:post-like:post:1:user:2"))
                .thenReturn("kite-universe:lock:community:post-like:post:1:user:2");
        when(redissonClient.getLock("kite-universe:lock:community:post-like:post:1:user:2")).thenReturn(lock);
        when(lock.tryLock(1500L, 5000L, TimeUnit.MILLISECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        RedissonDistributedLockService service =
                new RedissonDistributedLockService(redissonClient, properties, redisKeyManager);

        String result = service.executeWithLock("community:post-like:post:1:user:2", () -> "ok");

        assertEquals("ok", result);
        verify(lock).unlock();
    }

    @Test
    void executeWithLockShouldFailWhenLockIsBusy() throws Exception {
        RedissonClient redissonClient = Mockito.mock(RedissonClient.class);
        RedisKeyManager redisKeyManager = Mockito.mock(RedisKeyManager.class);
        RLock lock = Mockito.mock(RLock.class);

        RedisLockProperties properties = new RedisLockProperties();
        properties.setKeyPrefix("lock");

        when(redisKeyManager.buildKey("lock:user-progress:user:5"))
                .thenReturn("kite-universe:lock:user-progress:user:5");
        when(redissonClient.getLock("kite-universe:lock:user-progress:user:5")).thenReturn(lock);
        when(lock.tryLock(1500L, 5000L, TimeUnit.MILLISECONDS)).thenReturn(false);

        RedissonDistributedLockService service =
                new RedissonDistributedLockService(redissonClient, properties, redisKeyManager);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.executeWithLock("user-progress:user:5", () -> "ignored"));

        assertEquals(500, exception.getCode());
        verify(lock, never()).unlock();
    }
}
