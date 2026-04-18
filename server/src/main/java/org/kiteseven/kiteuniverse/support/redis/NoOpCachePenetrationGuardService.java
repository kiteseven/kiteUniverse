package org.kiteseven.kiteuniverse.support.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * No-op fallback used when Bloom-filter protection is disabled.
 */
@Service
@ConditionalOnProperty(prefix = "kite-universe.redis.bloom-filter", name = "enabled", havingValue = "false")
public class NoOpCachePenetrationGuardService implements CachePenetrationGuardService {

    @Override
    public boolean mightContainUserId(Long userId) {
        return true;
    }

    @Override
    public boolean mightContainPostId(Long postId) {
        return true;
    }

    @Override
    public void addUserId(Long userId) {
    }

    @Override
    public void addPostId(Long postId) {
    }

    @Override
    public void addUserIds(Collection<Long> userIds) {
    }

    @Override
    public void addPostIds(Collection<Long> postIds) {
    }
}
