package org.kiteseven.kiteuniverse.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the shared Redis key prefix used to isolate this project's cache data.
 */
@ConfigurationProperties(prefix = "kite-universe.redis")
public class RedisKeyProperties {

    /**
     * Project-level Redis key prefix used by all manually managed keys.
     */
    private String keyPrefix = "kite-universe";

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }
}
