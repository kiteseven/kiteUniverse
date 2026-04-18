package org.kiteseven.kiteuniverse.support.redis;

import org.kiteseven.kiteuniverse.mapper.CommunityPostMapper;
import org.kiteseven.kiteuniverse.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Warms Bloom filters with the ids that already exist in MySQL.
 */
@Component
@ConditionalOnProperty(prefix = "kite-universe.redis.bloom-filter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CachePenetrationBloomFilterInitializer {

    private static final Logger log = LoggerFactory.getLogger(CachePenetrationBloomFilterInitializer.class);

    private final CachePenetrationGuardService cachePenetrationGuardService;
    private final UserMapper userMapper;
    private final CommunityPostMapper communityPostMapper;

    public CachePenetrationBloomFilterInitializer(CachePenetrationGuardService cachePenetrationGuardService,
                                                  UserMapper userMapper,
                                                  CommunityPostMapper communityPostMapper) {
        this.cachePenetrationGuardService = cachePenetrationGuardService;
        this.userMapper = userMapper;
        this.communityPostMapper = communityPostMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        List<Long> userIds = userMapper.selectAllIds();
        List<Long> postIds = communityPostMapper.selectAllPublishedIds();

        cachePenetrationGuardService.addUserIds(userIds);
        cachePenetrationGuardService.addPostIds(postIds);

        log.info("Bloom filters warmed with {} user ids and {} published post ids", userIds.size(), postIds.size());
    }
}
