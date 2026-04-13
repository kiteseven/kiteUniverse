package org.kiteseven.kiteuniverse.support.community;

/**
 * Centralized unprefixed Redis key suffixes for community content pages.
 */
public final class CommunityContentCacheKeys {

    /**
     * Homepage cache key suffix.
     */
    public static final String HOME_PAGE = "content:home-page:v3";

    /**
     * Boards-page cache key suffix.
     */
    public static final String BOARDS_PAGE = "content:boards-page:v3";

    private CommunityContentCacheKeys() {
    }
}
