package org.kiteseven.kiteuniverse.support.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares two-level cache read/write behavior or cache invalidation for a method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TwoLevelCache {

    /**
     * Cache mode. Read/write methods use one key while eviction methods can invalidate multiple keys.
     */
    Mode mode() default Mode.READ_WRITE;

    /**
     * Cache key suffix or SpEL expression that resolves to one cache key.
     */
    String key() default "";

    /**
     * Cache key suffixes or SpEL expressions resolved after a successful write.
     */
    String[] evictKeys() default {};

    /**
     * Local Caffeine TTL in seconds. Negative values fall back to the shared default.
     */
    long localTtlSeconds() default -1L;

    /**
     * Optional local Caffeine TTL expression. When set, it overrides the numeric local TTL.
     */
    String localTtlExpression() default "";

    /**
     * Redis TTL in seconds. Negative values fall back to the shared default.
     */
    long redisTtlSeconds() default -1L;

    /**
     * Optional Redis TTL expression. When set, it overrides the numeric Redis TTL.
     */
    String redisTtlExpression() default "";

    /**
     * Whether null results should also be cached.
     */
    boolean cacheNullValue() default false;

    enum Mode {
        READ_WRITE,
        EVICT
    }
}
