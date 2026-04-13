package org.kiteseven.kiteuniverse.support.auth;

import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.support.redis.RedisKeyManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Stores authentication-related cache data in Redis.
 */
@Component
public class AuthCacheService {

    private static final String SMS_CODE_KEY_PREFIX = "auth:sms:";
    private static final String SMS_COOLDOWN_KEY_PREFIX = "auth:sms:cooldown:";
    private static final String SMS_DAILY_COUNT_KEY_PREFIX = "auth:sms:daily:";
    private static final String SMS_IP_WINDOW_KEY_PREFIX = "auth:sms:ip:";
    private static final String ACTIVE_SESSION_KEY_PREFIX = "auth:session:active:";
    private static final String USER_CURRENT_SESSION_KEY_PREFIX = "auth:user:session:";
    private static final String DEFAULT_MARKER_VALUE = "1";

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisKeyManager redisKeyManager;

    public AuthCacheService(StringRedisTemplate stringRedisTemplate, RedisKeyManager redisKeyManager) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisKeyManager = redisKeyManager;
    }

    /**
     * Caches the latest SMS verification code for a phone and business scene.
     *
     * @param phone phone number
     * @param scene business scene
     * @param code verification code
     * @param expireSeconds cache lifetime in seconds
     */
    public void cacheSmsCode(String phone, String scene, String code, long expireSeconds) {
        execute(() -> {
            stringRedisTemplate.opsForValue().set(
                    buildSmsCodeKey(phone, scene),
                    code,
                    Duration.ofSeconds(Math.max(expireSeconds, 1L))
            );
            return null;
        }, "短信验证码缓存写入失败");
    }

    /**
     * Reads the latest cached SMS verification code for the specified scene.
     *
     * @param phone phone number
     * @param scene business scene
     * @return cached code or null when missing
     */
    public String getSmsCode(String phone, String scene) {
        return execute(
                () -> stringRedisTemplate.opsForValue().get(buildSmsCodeKey(phone, scene)),
                "短信验证码缓存读取失败"
        );
    }

    /**
     * Removes the cached SMS verification code after it is used or invalidated.
     *
     * @param phone phone number
     * @param scene business scene
     */
    public void clearSmsCode(String phone, String scene) {
        execute(() -> {
            stringRedisTemplate.delete(buildSmsCodeKey(phone, scene));
            return null;
        }, "短信验证码缓存删除失败");
    }

    /**
     * Tries to acquire the SMS send cooldown for a phone and business scene.
     *
     * @param phone phone number
     * @param scene business scene
     * @param cooldownSeconds cooldown duration in seconds
     * @return true when the current request can continue sending
     */
    public boolean tryAcquireSmsSendCooldown(String phone, String scene, long cooldownSeconds) {
        if (cooldownSeconds <= 0L) {
            return true;
        }

        Boolean acquired = execute(
                () -> stringRedisTemplate.opsForValue().setIfAbsent(
                        buildSmsCooldownKey(phone, scene),
                        DEFAULT_MARKER_VALUE,
                        Duration.ofSeconds(cooldownSeconds)
                ),
                "短信发送频控缓存写入失败"
        );
        return Boolean.TRUE.equals(acquired);
    }

    /**
     * Returns the remaining cooldown time for the specified phone and scene.
     *
     * @param phone phone number
     * @param scene business scene
     * @return remaining cooldown seconds
     */
    public long getSmsSendCooldownSeconds(String phone, String scene) {
        Long remainingSeconds = execute(
                () -> stringRedisTemplate.getExpire(buildSmsCooldownKey(phone, scene)),
                "短信发送频控缓存读取失败"
        );
        if (remainingSeconds == null || remainingSeconds < 0L) {
            return 0L;
        }
        return remainingSeconds;
    }

    /**
     * Increments the current daily SMS send count for a phone number and returns the latest count.
     *
     * @param phone phone number
     * @return latest daily count
     */
    public long incrementSmsDailyCount(String phone) {
        return execute(() -> {
            String key = buildSmsDailyCountKey(phone);
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                stringRedisTemplate.expire(key, resolveDurationUntilTomorrow());
            }
            return count == null ? 0L : count;
        }, "短信日限额缓存写入失败");
    }

    /**
     * Increments the current per-IP SMS count within the configured window and returns the latest count.
     *
     * @param clientIp client IP address
     * @param windowSeconds rate-limit window in seconds
     * @return latest window count
     */
    public long incrementSmsIpWindowCount(String clientIp, long windowSeconds) {
        return execute(() -> {
            String key = buildSmsIpWindowKey(clientIp);
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L && windowSeconds > 0L) {
                stringRedisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }
            return count == null ? 0L : count;
        }, "短信 IP 限流缓存写入失败");
    }

    /**
     * Returns the remaining TTL for the current per-IP SMS rate-limit window.
     *
     * @param clientIp client IP address
     * @return remaining window seconds
     */
    public long getSmsIpWindowRemainingSeconds(String clientIp) {
        Long remainingSeconds = execute(
                () -> stringRedisTemplate.getExpire(buildSmsIpWindowKey(clientIp)),
                "短信 IP 限流缓存读取失败"
        );
        if (remainingSeconds == null || remainingSeconds < 0L) {
            return 0L;
        }
        return remainingSeconds;
    }

    /**
     * Replaces the current user session and removes the previous active device.
     *
     * @param userId user id
     * @param sessionId session id stored inside the signed token
     * @param ttlMillis session lifetime in milliseconds
     */
    public void replaceUserSession(Long userId, String sessionId, long ttlMillis) {
        if (ttlMillis <= 0L) {
            return;
        }

        execute(() -> {
            String currentUserKey = buildUserCurrentSessionKey(userId);
            String previousSessionId = stringRedisTemplate.opsForValue().get(currentUserKey);

            if (StringUtils.hasText(previousSessionId) && !previousSessionId.equals(sessionId)) {
                stringRedisTemplate.delete(buildActiveSessionKey(previousSessionId));
            }

            Duration ttl = Duration.ofMillis(ttlMillis);
            stringRedisTemplate.opsForValue().set(
                    buildActiveSessionKey(sessionId),
                    String.valueOf(userId),
                    ttl
            );
            stringRedisTemplate.opsForValue().set(
                    currentUserKey,
                    sessionId,
                    ttl
            );
            return null;
        }, "登录态缓存写入失败");
    }

    /**
     * Returns whether the specified session is still the current active device for a user.
     *
     * @param userId user id
     * @param sessionId session id stored inside the token
     * @return true when the session is valid
     */
    public boolean isSessionActive(Long userId, String sessionId) {
        return execute(() -> {
            String currentSessionId = stringRedisTemplate.opsForValue().get(buildUserCurrentSessionKey(userId));
            if (!StringUtils.hasText(currentSessionId) || !currentSessionId.equals(sessionId)) {
                return false;
            }

            String activeUserId = stringRedisTemplate.opsForValue().get(buildActiveSessionKey(sessionId));
            return String.valueOf(userId).equals(activeUserId);
        }, "登录态缓存读取失败");
    }

    /**
     * Refreshes the TTL of the active session and its current-user pointer.
     *
     * @param userId user id
     * @param sessionId session id stored inside the token
     * @param ttlMillis renewed lifetime in milliseconds
     */
    public void refreshUserSession(Long userId, String sessionId, long ttlMillis) {
        if (ttlMillis <= 0L) {
            return;
        }

        execute(() -> {
            Duration ttl = Duration.ofMillis(ttlMillis);
            stringRedisTemplate.opsForValue().set(
                    buildActiveSessionKey(sessionId),
                    String.valueOf(userId),
                    ttl
            );
            stringRedisTemplate.opsForValue().set(
                    buildUserCurrentSessionKey(userId),
                    sessionId,
                    ttl
            );
            return null;
        }, "登录态续期失败");
    }

    /**
     * Removes the active session and clears the user pointer when it still matches.
     *
     * @param userId user id
     * @param sessionId session id stored inside the token
     */
    public void removeUserSession(Long userId, String sessionId) {
        execute(() -> {
            stringRedisTemplate.delete(buildActiveSessionKey(sessionId));

            String userKey = buildUserCurrentSessionKey(userId);
            String currentSessionId = stringRedisTemplate.opsForValue().get(userKey);
            if (StringUtils.hasText(currentSessionId) && currentSessionId.equals(sessionId)) {
                stringRedisTemplate.delete(userKey);
            }
            return null;
        }, "登录态缓存删除失败");
    }

    /**
     * Builds the Redis key used for the latest SMS code in a scene.
     *
     * @param phone phone number
     * @param scene business scene
     * @return redis key
     */
    private String buildSmsCodeKey(String phone, String scene) {
        return redisKeyManager.buildKey(SMS_CODE_KEY_PREFIX + scene + ":" + phone);
    }

    /**
     * Builds the Redis key used for the SMS send cooldown window.
     *
     * @param phone phone number
     * @param scene business scene
     * @return redis key
     */
    private String buildSmsCooldownKey(String phone, String scene) {
        return redisKeyManager.buildKey(SMS_COOLDOWN_KEY_PREFIX + scene + ":" + phone);
    }

    /**
     * Builds the Redis key used for the daily SMS send counter.
     *
     * @param phone phone number
     * @return redis key
     */
    private String buildSmsDailyCountKey(String phone) {
        return redisKeyManager.buildKey(SMS_DAILY_COUNT_KEY_PREFIX + phone + ":" + LocalDate.now(ZoneId.systemDefault()));
    }

    /**
     * Builds the Redis key used for the current IP-based SMS window counter.
     *
     * @param clientIp client IP address
     * @return redis key
     */
    private String buildSmsIpWindowKey(String clientIp) {
        return redisKeyManager.buildKey(SMS_IP_WINDOW_KEY_PREFIX + clientIp);
    }

    /**
     * Builds the Redis key used for an active session.
     *
     * @param sessionId session id
     * @return redis key
     */
    private String buildActiveSessionKey(String sessionId) {
        return redisKeyManager.buildKey(ACTIVE_SESSION_KEY_PREFIX + sessionId);
    }

    /**
     * Builds the Redis key used for the current device session of a user.
     *
     * @param userId user id
     * @return redis key
     */
    private String buildUserCurrentSessionKey(Long userId) {
        return redisKeyManager.buildKey(USER_CURRENT_SESSION_KEY_PREFIX + userId);
    }

    /**
     * Resolves the TTL used by the daily SMS counter so it clears at the next local midnight.
     *
     * @return duration until tomorrow
     */
    private Duration resolveDurationUntilTomorrow() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        LocalDateTime tomorrow = now.toLocalDate().plusDays(1).atStartOfDay();
        return Duration.between(now, tomorrow);
    }

    /**
     * Executes a Redis operation and wraps cache-layer errors with a business exception.
     *
     * @param callback redis callback
     * @param errorMessage user-friendly error message
     * @param <T> return type
     * @return callback result
     */
    private <T> T execute(CacheCallback<T> callback, String errorMessage) {
        try {
            return callback.execute();
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, errorMessage);
        }
    }

    /**
     * Functional interface used to wrap Redis operations.
     *
     * @param <T> return type
     */
    @FunctionalInterface
    private interface CacheCallback<T> {

        /**
         * Executes the Redis operation.
         *
         * @return operation result
         */
        T execute();
    }
}
