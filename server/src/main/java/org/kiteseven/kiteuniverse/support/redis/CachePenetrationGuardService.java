package org.kiteseven.kiteuniverse.support.redis;

import java.util.Collection;

/**
 * Provides ID Bloom-filter operations used to block obvious cache-penetration attacks.
 */
public interface CachePenetrationGuardService {

    /**
     * Returns whether the user id may exist.
     *
     * @param userId user id
     * @return true when the request should continue to cache/database lookup
     */
    boolean mightContainUserId(Long userId);

    /**
     * Returns whether the post id may exist.
     *
     * @param postId post id
     * @return true when the request should continue to cache/database lookup
     */
    boolean mightContainPostId(Long postId);

    /**
     * Adds a newly created user id to the Bloom filter.
     *
     * @param userId user id
     */
    void addUserId(Long userId);

    /**
     * Adds a newly created post id to the Bloom filter.
     *
     * @param postId post id
     */
    void addPostId(Long postId);

    /**
     * Bulk-loads existing user ids during application warm-up.
     *
     * @param userIds known user ids
     */
    void addUserIds(Collection<Long> userIds);

    /**
     * Bulk-loads existing post ids during application warm-up.
     *
     * @param postIds known post ids
     */
    void addPostIds(Collection<Long> postIds);
}
