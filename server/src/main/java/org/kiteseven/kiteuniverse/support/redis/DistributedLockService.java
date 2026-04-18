package org.kiteseven.kiteuniverse.support.redis;

import java.util.function.Supplier;

/**
 * Serializes hot write paths with Redis-backed distributed locks.
 */
public interface DistributedLockService {

    /**
     * Executes the given action while holding the specified lock.
     *
     * @param lockKey business lock key without the shared project prefix
     * @param action code to run after the lock is acquired
     * @param <T> result type
     * @return action result
     */
    <T> T executeWithLock(String lockKey, Supplier<T> action);

    /**
     * Executes the given action while holding the specified lock.
     *
     * @param lockKey business lock key without the shared project prefix
     * @param action code to run after the lock is acquired
     */
    void runWithLock(String lockKey, Runnable action);
}
