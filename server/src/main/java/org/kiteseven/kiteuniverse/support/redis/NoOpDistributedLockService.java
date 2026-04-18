package org.kiteseven.kiteuniverse.support.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * No-op fallback used when distributed locking is explicitly disabled.
 */
@Service
@ConditionalOnProperty(prefix = "kite-universe.redis.lock", name = "enabled", havingValue = "false")
public class NoOpDistributedLockService implements DistributedLockService {

    @Override
    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        return action.get();
    }

    @Override
    public void runWithLock(String lockKey, Runnable action) {
        action.run();
    }
}
