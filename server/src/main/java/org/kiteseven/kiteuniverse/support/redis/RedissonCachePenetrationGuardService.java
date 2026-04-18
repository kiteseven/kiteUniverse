package org.kiteseven.kiteuniverse.support.redis;

import jakarta.annotation.PostConstruct;
import org.kiteseven.kiteuniverse.config.properties.RedisBloomFilterProperties;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Redisson-backed Bloom filters for user and post ids.
 */
@Service
@ConditionalOnProperty(prefix = "kite-universe.redis.bloom-filter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedissonCachePenetrationGuardService implements CachePenetrationGuardService {

    private static final Logger log = LoggerFactory.getLogger(RedissonCachePenetrationGuardService.class);

    private final RedissonClient redissonClient;
    private final RedisBloomFilterProperties bloomFilterProperties;
    private final RedisKeyManager redisKeyManager;

    private RBloomFilter<Long> userBloomFilter;
    private RBloomFilter<Long> postBloomFilter;

    private final AtomicBoolean userFilterReady = new AtomicBoolean(false);
    private final AtomicBoolean postFilterReady = new AtomicBoolean(false);

    public RedissonCachePenetrationGuardService(RedissonClient redissonClient,
                                                RedisBloomFilterProperties bloomFilterProperties,
                                                RedisKeyManager redisKeyManager) {
        this.redissonClient = redissonClient;
        this.bloomFilterProperties = bloomFilterProperties;
        this.redisKeyManager = redisKeyManager;
    }

    @PostConstruct
    void prepareFilters() {
        this.userBloomFilter = redissonClient.getBloomFilter(resolveRedisKey(bloomFilterProperties.getUser().getKey()));
        this.postBloomFilter = redissonClient.getBloomFilter(resolveRedisKey(bloomFilterProperties.getPost().getKey()));
    }

    @Override
    public boolean mightContainUserId(Long userId) {
        if (userId == null || userId <= 0L) {
            return false;
        }
        return mightContain(userBloomFilter, bloomFilterProperties.getUser(), userFilterReady, userId, "user");
    }

    @Override
    public boolean mightContainPostId(Long postId) {
        if (postId == null || postId <= 0L) {
            return false;
        }
        return mightContain(postBloomFilter, bloomFilterProperties.getPost(), postFilterReady, postId, "post");
    }

    @Override
    public void addUserId(Long userId) {
        add(userBloomFilter, bloomFilterProperties.getUser(), userFilterReady, userId, "user");
    }

    @Override
    public void addPostId(Long postId) {
        add(postBloomFilter, bloomFilterProperties.getPost(), postFilterReady, postId, "post");
    }

    @Override
    public void addUserIds(Collection<Long> userIds) {
        addAll(userBloomFilter, bloomFilterProperties.getUser(), userFilterReady, userIds, "user");
    }

    @Override
    public void addPostIds(Collection<Long> postIds) {
        addAll(postBloomFilter, bloomFilterProperties.getPost(), postFilterReady, postIds, "post");
    }

    private boolean mightContain(RBloomFilter<Long> bloomFilter,
                                 RedisBloomFilterProperties.FilterSpec filterSpec,
                                 AtomicBoolean filterReady,
                                 Long id,
                                 String label) {
        if (!ensureInitialized(bloomFilter, filterSpec, filterReady, label)) {
            return true;
        }
        try {
            return bloomFilter.contains(id);
        } catch (Exception exception) {
            log.warn("Bloom filter lookup skipped for {} id {}", label, id, exception);
            return true;
        }
    }

    private void add(RBloomFilter<Long> bloomFilter,
                     RedisBloomFilterProperties.FilterSpec filterSpec,
                     AtomicBoolean filterReady,
                     Long id,
                     String label) {
        if (id == null || id <= 0L) {
            return;
        }
        if (!ensureInitialized(bloomFilter, filterSpec, filterReady, label)) {
            return;
        }
        try {
            bloomFilter.add(id);
        } catch (Exception exception) {
            log.warn("Bloom filter update skipped for {} id {}", label, id, exception);
        }
    }

    private void addAll(RBloomFilter<Long> bloomFilter,
                        RedisBloomFilterProperties.FilterSpec filterSpec,
                        AtomicBoolean filterReady,
                        Collection<Long> ids,
                        String label) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        if (!ensureInitialized(bloomFilter, filterSpec, filterReady, label)) {
            return;
        }
        for (Long id : ids) {
            if (id == null || id <= 0L) {
                continue;
            }
            try {
                bloomFilter.add(id);
            } catch (Exception exception) {
                log.warn("Bloom filter batch update skipped for {} id {}", label, id, exception);
                return;
            }
        }
    }

    private boolean ensureInitialized(RBloomFilter<Long> bloomFilter,
                                      RedisBloomFilterProperties.FilterSpec filterSpec,
                                      AtomicBoolean filterReady,
                                      String label) {
        if (filterReady.get()) {
            return true;
        }
        try {
            bloomFilter.tryInit(resolveExpectedInsertions(filterSpec), resolveFalseProbability(filterSpec));
            filterReady.set(true);
            return true;
        } catch (Exception exception) {
            log.warn("Bloom filter initialization skipped for {}", label, exception);
            return false;
        }
    }

    private long resolveExpectedInsertions(RedisBloomFilterProperties.FilterSpec filterSpec) {
        return Math.max(filterSpec.getExpectedInsertions(), 1L);
    }

    private double resolveFalseProbability(RedisBloomFilterProperties.FilterSpec filterSpec) {
        double configuredProbability = filterSpec.getFalseProbability();
        if (configuredProbability <= 0D || configuredProbability >= 1D) {
            return 0.001D;
        }
        return configuredProbability;
    }

    private String resolveRedisKey(String configuredKey) {
        String key = StringUtils.hasText(configuredKey) ? configuredKey.trim() : "bloom-filter:ids";
        return redisKeyManager.buildKey(key);
    }
}
