package org.kiteseven.kiteuniverse.support.redis;

import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.config.properties.RedisLockProperties;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redisson-backed distributed lock implementation for hot write paths.
 */
@Service
@ConditionalOnProperty(prefix = "kite-universe.redis.lock", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedissonDistributedLockService implements DistributedLockService {

    private static final Logger log = LoggerFactory.getLogger(RedissonDistributedLockService.class);

    private final RedissonClient redissonClient;
    private final RedisLockProperties redisLockProperties;
    private final RedisKeyManager redisKeyManager;

    public RedissonDistributedLockService(RedissonClient redissonClient,
                                          RedisLockProperties redisLockProperties,
                                          RedisKeyManager redisKeyManager) {
        this.redissonClient = redissonClient;
        this.redisLockProperties = redisLockProperties;
        this.redisKeyManager = redisKeyManager;
    }

    @Override
    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        RLock lock = redissonClient.getLock(resolveLockKey(lockKey));
        boolean locked = false;
        try {
            locked = lock.tryLock(resolveWaitTimeMillis(), resolveLeaseTimeMillis(), TimeUnit.MILLISECONDS);
            if (!locked) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "Operation is already in progress, please retry");
            }
            return action.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("Distributed lock acquisition interrupted for key {}", lockKey, exception);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "Unable to acquire distributed lock, please retry");
        } finally {
            releaseLock(lock, locked, lockKey);
        }
    }

    @Override
    public void runWithLock(String lockKey, Runnable action) {
        executeWithLock(lockKey, () -> {
            action.run();
            return null;
        });
    }

    private void releaseLock(RLock lock, boolean locked, String lockKey) {
        if (!locked) {
            return;
        }
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (Exception exception) {
            log.warn("Distributed lock release skipped for key {}", lockKey, exception);
        }
    }

    private String resolveLockKey(String lockKey) {
        String namespace = StringUtils.hasText(redisLockProperties.getKeyPrefix())
                ? redisLockProperties.getKeyPrefix().trim()
                : "lock";
        String normalizedLockKey = StringUtils.hasText(lockKey) ? lockKey.trim() : "default";
        return redisKeyManager.buildKey(namespace + ":" + normalizedLockKey);
    }

    private long resolveWaitTimeMillis() {
        return Math.max(redisLockProperties.getWaitTimeMillis(), 0L);
    }

    private long resolveLeaseTimeMillis() {
        return Math.max(redisLockProperties.getLeaseTimeMillis(), 1000L);
    }
}
