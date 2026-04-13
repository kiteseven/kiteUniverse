package org.kiteseven.kiteuniverse.support.redis;

import org.kiteseven.kiteuniverse.config.properties.RedisKeyProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Builds Redis keys with a shared project prefix so different projects do not collide.
 */
@Component
public class RedisKeyManager {

    private final RedisKeyProperties redisKeyProperties;

    public RedisKeyManager(RedisKeyProperties redisKeyProperties) {
        this.redisKeyProperties = redisKeyProperties;
    }

    /**
     * Builds a prefixed Redis key from a project-local suffix.
     *
     * @param keySuffix unprefixed business key suffix
     * @return Redis key with the shared project prefix applied
     */
    public String buildKey(String keySuffix) {
        String normalizedSuffix = normalizeKeyPart(keySuffix);
        String normalizedPrefix = normalizeKeyPart(redisKeyProperties.getKeyPrefix());

        if (!StringUtils.hasText(normalizedPrefix)) {
            return normalizedSuffix;
        }
        if (!StringUtils.hasText(normalizedSuffix)) {
            return normalizedPrefix;
        }
        return normalizedPrefix + ":" + normalizedSuffix;
    }

    /**
     * Normalizes a key segment by trimming whitespace and redundant separators.
     *
     * @param value key segment
     * @return normalized key segment
     */
    private String normalizeKeyPart(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        String normalizedValue = value.trim();
        while (normalizedValue.startsWith(":")) {
            normalizedValue = normalizedValue.substring(1);
        }
        while (normalizedValue.endsWith(":")) {
            normalizedValue = normalizedValue.substring(0, normalizedValue.length() - 1);
        }
        return normalizedValue;
    }
}
