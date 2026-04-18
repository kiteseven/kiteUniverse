package org.kiteseven.kiteuniverse.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds Redisson Bloom filter settings used to prevent cache penetration.
 */
@ConfigurationProperties(prefix = "kite-universe.redis.bloom-filter")
public class RedisBloomFilterProperties {

    /**
     * Enables or disables Bloom-filter-based cache penetration protection.
     */
    private boolean enabled = true;

    /**
     * Filter settings for user ids.
     */
    private FilterSpec user = new FilterSpec();

    /**
     * Filter settings for published post ids.
     */
    private FilterSpec post = new FilterSpec("bloom-filter:post-ids", 200000L, 0.001D);

    public RedisBloomFilterProperties() {
        this.user = new FilterSpec("bloom-filter:user-ids", 200000L, 0.001D);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public FilterSpec getUser() {
        return user;
    }

    public void setUser(FilterSpec user) {
        this.user = user;
    }

    public FilterSpec getPost() {
        return post;
    }

    public void setPost(FilterSpec post) {
        this.post = post;
    }

    /**
     * Represents one Bloom filter's Redis key and sizing hints.
     */
    public static class FilterSpec {

        private String key;

        private long expectedInsertions = 200000L;

        private double falseProbability = 0.001D;

        public FilterSpec() {
        }

        public FilterSpec(String key, long expectedInsertions, double falseProbability) {
            this.key = key;
            this.expectedInsertions = expectedInsertions;
            this.falseProbability = falseProbability;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public long getExpectedInsertions() {
            return expectedInsertions;
        }

        public void setExpectedInsertions(long expectedInsertions) {
            this.expectedInsertions = expectedInsertions;
        }

        public double getFalseProbability() {
            return falseProbability;
        }

        public void setFalseProbability(double falseProbability) {
            this.falseProbability = falseProbability;
        }
    }
}
