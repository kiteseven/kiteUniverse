package org.kiteseven.kiteuniverse.support.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Listens for Redis pub/sub invalidation events and clears local Caffeine entries.
 */
@Component
@ConditionalOnProperty(prefix = "kite-universe.cache.two-level", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TwoLevelCacheMessageListener implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(TwoLevelCacheMessageListener.class);

    private final TwoLevelCacheService twoLevelCacheService;

    public TwoLevelCacheMessageListener(TwoLevelCacheService twoLevelCacheService) {
        this.twoLevelCacheService = twoLevelCacheService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String fullCacheKey = new String(message.getBody(), StandardCharsets.UTF_8);
            twoLevelCacheService.evictLocalByFullKey(fullCacheKey);
        } catch (Exception exception) {
            log.warn("Two-level invalidation message handling skipped", exception);
        }
    }
}
