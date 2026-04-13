package org.kiteseven.kiteuniverse.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds Redis cache settings used by the community homepage and boards page.
 */
@ConfigurationProperties(prefix = "kite-universe.content")
public class CommunityContentProperties {

    /**
     * Redis TTL for the homepage payload in seconds.
     */
    private long homeCacheSeconds = 300L;

    /**
     * Redis TTL for the boards payload in seconds.
     */
    private long boardsCacheSeconds = 300L;

    public long getHomeCacheSeconds() {
        return homeCacheSeconds;
    }

    public void setHomeCacheSeconds(long homeCacheSeconds) {
        this.homeCacheSeconds = homeCacheSeconds;
    }

    public long getBoardsCacheSeconds() {
        return boardsCacheSeconds;
    }

    public void setBoardsCacheSeconds(long boardsCacheSeconds) {
        this.boardsCacheSeconds = boardsCacheSeconds;
    }
}
