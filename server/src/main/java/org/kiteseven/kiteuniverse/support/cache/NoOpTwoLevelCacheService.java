package org.kiteseven.kiteuniverse.support.cache;

import com.fasterxml.jackson.databind.JavaType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * No-op fallback used when two-level cache support is disabled.
 */
@Service
@ConditionalOnProperty(prefix = "kite-universe.cache.two-level", name = "enabled", havingValue = "false")
public class NoOpTwoLevelCacheService implements TwoLevelCacheService {

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public Object get(String keySuffix, JavaType javaType, Duration localTtl) {
        return null;
    }

    @Override
    public void put(String keySuffix, Object value, Duration localTtl, Duration redisTtl) {
    }

    @Override
    public void evict(String keySuffix) {
    }

    @Override
    public void evictLocalByFullKey(String fullCacheKey) {
    }
}
