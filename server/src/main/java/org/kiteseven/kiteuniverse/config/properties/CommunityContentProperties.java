package org.kiteseven.kiteuniverse.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds homepage and boards-page TTL settings used by the two-level cache layer.
 */
@ConfigurationProperties(prefix = "kite-universe.content")
@Component
public class CommunityContentProperties {

    /**
     * Redis TTL for the homepage payload in seconds.
     */
    private long homeCacheSeconds = 300L;

    /**
     * Caffeine TTL for the homepage payload in seconds.
     */
    private long homeLocalCacheSeconds = 30L;

    /**
     * Redis TTL for the boards payload in seconds.
     */
    private long boardsCacheSeconds = 300L;

    /**
     * Caffeine TTL for the boards payload in seconds.
     */
    private long boardsLocalCacheSeconds = 30L;

    public long getHomeCacheSeconds() {
        return homeCacheSeconds;
    }

    public void setHomeCacheSeconds(long homeCacheSeconds) {
        this.homeCacheSeconds = homeCacheSeconds;
    }

    public long getHomeLocalCacheSeconds() {
        return homeLocalCacheSeconds;
    }

    public void setHomeLocalCacheSeconds(long homeLocalCacheSeconds) {
        this.homeLocalCacheSeconds = homeLocalCacheSeconds;
    }

    public long getBoardsCacheSeconds() {
        return boardsCacheSeconds;
    }

    public void setBoardsCacheSeconds(long boardsCacheSeconds) {
        this.boardsCacheSeconds = boardsCacheSeconds;
    }

    public long getBoardsLocalCacheSeconds() {
        return boardsLocalCacheSeconds;
    }

    public void setBoardsLocalCacheSeconds(long boardsLocalCacheSeconds) {
        this.boardsLocalCacheSeconds = boardsLocalCacheSeconds;
    }
}
