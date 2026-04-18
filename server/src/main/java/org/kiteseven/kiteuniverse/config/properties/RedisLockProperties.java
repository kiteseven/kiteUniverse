package org.kiteseven.kiteuniverse.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds Redis distributed-lock settings used by Redisson.
 */
@ConfigurationProperties(prefix = "kite-universe.redis.lock")
public class RedisLockProperties {

    /**
     * Enables or disables Redisson-based distributed locking.
     */
    private boolean enabled = true;

    /**
     * Namespace appended after the shared Redis key prefix.
     */
    private String keyPrefix = "lock";

    /**
     * Maximum time to wait for a lock before failing the request.
     */
    private long waitTimeMillis = 1500L;

    /**
     * Maximum time to keep the lock if the owner crashes before unlock.
     */
    private long leaseTimeMillis = 5000L;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public long getWaitTimeMillis() {
        return waitTimeMillis;
    }

    public void setWaitTimeMillis(long waitTimeMillis) {
        this.waitTimeMillis = waitTimeMillis;
    }

    public long getLeaseTimeMillis() {
        return leaseTimeMillis;
    }

    public void setLeaseTimeMillis(long leaseTimeMillis) {
        this.leaseTimeMillis = leaseTimeMillis;
    }
}
