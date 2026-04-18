package org.kiteseven.kiteuniverse.config;

import org.kiteseven.kiteuniverse.config.properties.TwoLevelCacheProperties;
import org.kiteseven.kiteuniverse.support.cache.TwoLevelCacheMessageListener;
import org.kiteseven.kiteuniverse.support.redis.RedisKeyManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Configures Redis pub/sub used to fan out local-cache invalidations across instances.
 */
@Configuration
@ConditionalOnProperty(prefix = "kite-universe.cache.two-level", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TwoLevelCacheConfig {

    @Bean
    public ChannelTopic twoLevelCacheInvalidationTopic(TwoLevelCacheProperties twoLevelCacheProperties,
                                                       RedisKeyManager redisKeyManager) {
        return new ChannelTopic(redisKeyManager.buildKey(twoLevelCacheProperties.getInvalidationTopic()));
    }

    @Bean
    public RedisMessageListenerContainer twoLevelCacheMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            TwoLevelCacheMessageListener twoLevelCacheMessageListener,
            ChannelTopic twoLevelCacheInvalidationTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(twoLevelCacheMessageListener, twoLevelCacheInvalidationTopic);
        return container;
    }
}
