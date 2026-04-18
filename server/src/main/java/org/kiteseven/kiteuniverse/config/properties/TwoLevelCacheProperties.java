package org.kiteseven.kiteuniverse.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds shared settings for the Caffeine + Redis two-level cache infrastructure.
 */
@ConfigurationProperties(prefix = "kite-universe.cache.two-level")
public class TwoLevelCacheProperties {

    /**
     * Enables or disables the two-level cache infrastructure.
     */
    private boolean enabled = true;

    /**
     * Default Caffeine entry TTL when a method does not override it.
     */
    private long defaultLocalTtlSeconds = 30L;

    /**
     * Default Redis entry TTL when a method does not override it.
     */
    private long defaultRedisTtlSeconds = 300L;

    /**
     * Maximum number of entries kept in the local cache.
     */
    private long localMaximumSize = 1000L;

    /**
     * Redis topic used to broadcast cross-instance invalidation messages.
     */
    private String invalidationTopic = "cache:two-level:invalidate";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getDefaultLocalTtlSeconds() {
        return defaultLocalTtlSeconds;
    }

    public void setDefaultLocalTtlSeconds(long defaultLocalTtlSeconds) {
        this.defaultLocalTtlSeconds = defaultLocalTtlSeconds;
    }

    public long getDefaultRedisTtlSeconds() {
        return defaultRedisTtlSeconds;
    }

    public void setDefaultRedisTtlSeconds(long defaultRedisTtlSeconds) {
        this.defaultRedisTtlSeconds = defaultRedisTtlSeconds;
    }

    public long getLocalMaximumSize() {
        return localMaximumSize;
    }

    public void setLocalMaximumSize(long localMaximumSize) {
        this.localMaximumSize = localMaximumSize;
    }

    public String getInvalidationTopic() {
        return invalidationTopic;
    }

    public void setInvalidationTopic(String invalidationTopic) {
        this.invalidationTopic = invalidationTopic;
    }
}
