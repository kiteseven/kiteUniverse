package org.kiteseven.kiteuniverse.support.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.config.properties.AuthTokenProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;

/**
 * Creates, parses, and revokes lightweight signed user tokens.
 */
@Component
public class UserTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String TOKEN_SEPARATOR = ".";
    private static final String PAYLOAD_SEPARATOR = ":";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthTokenProperties authTokenProperties;
    private final AuthCacheService authCacheService;
    private final SecureRandom secureRandom = new SecureRandom();

    public UserTokenService(AuthTokenProperties authTokenProperties, AuthCacheService authCacheService) {
        this.authTokenProperties = authTokenProperties;
        this.authCacheService = authCacheService;
    }

    /**
     * Generates a signed token for the specified user and stores it as the user's only active session.
     *
     * @param userId authenticated user id
     * @return token info used by the frontend
     */
    public AuthToken generateToken(Long userId) {
        long issuedAtMillis = System.currentTimeMillis();
        long expiresAtMillis = issuedAtMillis + authTokenProperties.getUserTtl();
        String sessionId = generateSessionId();
        String payload = encode(buildPayload(userId, sessionId, issuedAtMillis));
        String signature = sign(payload);
        String tokenValue = payload + TOKEN_SEPARATOR + signature;

        authCacheService.replaceUserSession(userId, sessionId, authTokenProperties.getUserTtl());

        return new AuthToken(
                tokenValue,
                Math.max(authTokenProperties.getUserTtl() / 1000L, 1L),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(expiresAtMillis), ZoneId.systemDefault())
        );
    }

    /**
     * Parses the token and returns the user id after signature and Redis-session checks.
     * Every successful request refreshes the session TTL to provide sliding expiration.
     *
     * @param token raw client token
     * @return authenticated user id
     */
    public Long parseUserId(String token) {
        String normalizedToken = normalizeToken(token);
        TokenPayload tokenPayload = parseToken(normalizedToken);

        if (!authCacheService.isSessionActive(tokenPayload.userId(), tokenPayload.sessionId())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录状态已失效，请重新登录");
        }

        authCacheService.refreshUserSession(
                tokenPayload.userId(),
                tokenPayload.sessionId(),
                authTokenProperties.getUserTtl()
        );
        return tokenPayload.userId();
    }

    /**
     * Revokes the current token by removing its Redis session.
     *
     * @param token raw client token
     */
    public void revokeToken(String token) {
        String normalizedToken = normalizeToken(token);
        TokenPayload tokenPayload = parseToken(normalizedToken);
        authCacheService.removeUserSession(tokenPayload.userId(), tokenPayload.sessionId());
    }

    /**
     * Resolves the token from the standard Authorization header or the configured custom header.
     *
     * @param request current HTTP request
     * @return raw token or null when missing
     */
    public String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length()).trim();
        }

        String customHeaderToken = request.getHeader(authTokenProperties.getUserTokenName());
        if (StringUtils.hasText(customHeaderToken)) {
            return customHeaderToken.trim();
        }
        return null;
    }

    /**
     * Parses the token payload after verifying the signature.
     *
     * @param token normalized token
     * @return parsed token payload
     */
    private TokenPayload parseToken(String token) {
        String[] parts = token.split("\\Q" + TOKEN_SEPARATOR + "\\E");
        if (parts.length != 2) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录凭证格式不正确");
        }

        String payload = parts[0];
        String signature = parts[1];
        String expectedSignature = sign(payload);

        if (!MessageDigest.isEqual(
                signature.getBytes(StandardCharsets.UTF_8),
                expectedSignature.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录凭证校验失败");
        }

        String decodedPayload = decode(payload);
        String[] fields = decodedPayload.split(PAYLOAD_SEPARATOR);
        if (fields.length != 3) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录凭证内容不完整");
        }

        long userId;
        try {
            userId = Long.parseLong(fields[0]);
        } catch (NumberFormatException exception) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录凭证用户信息无效");
        }

        String sessionId = fields[1];
        if (!StringUtils.hasText(sessionId)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录凭证会话信息无效");
        }

        try {
            Long.parseLong(fields[2]);
        } catch (NumberFormatException exception) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录凭证签发时间无效");
        }

        return new TokenPayload(userId, sessionId);
    }

    /**
     * Normalizes the raw token input and rejects missing credentials.
     *
     * @param token raw client token
     * @return trimmed token
     */
    private String normalizeToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未获取到登录凭证");
        }
        return token.trim();
    }

    /**
     * Builds the token payload string.
     *
     * @param userId user id
     * @param sessionId random session id
     * @param issuedAtMillis issue time in epoch milliseconds
     * @return token payload
     */
    private String buildPayload(Long userId, String sessionId, long issuedAtMillis) {
        return userId + PAYLOAD_SEPARATOR + sessionId + PAYLOAD_SEPARATOR + issuedAtMillis;
    }

    /**
     * Generates a cryptographically random session id for Redis-backed single-device login.
     *
     * @return URL-safe session id
     */
    private String generateSessionId() {
        byte[] sessionBytes = new byte[24];
        secureRandom.nextBytes(sessionBytes);
        return encode(sessionBytes);
    }

    /**
     * Signs a token payload with the configured HMAC secret.
     *
     * @param payload encoded payload
     * @return signature
     */
    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    authTokenProperties.getUserSecretKeyString().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return encode(signatureBytes);
        } catch (Exception exception) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "生成登录凭证失败");
        }
    }

    /**
     * Encodes a UTF-8 string with URL-safe Base64.
     *
     * @param value raw string
     * @return encoded value
     */
    private String encode(String value) {
        return encode(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encodes raw bytes with URL-safe Base64.
     *
     * @param value raw bytes
     * @return encoded value
     */
    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    /**
     * Decodes a Base64 URL-safe token payload.
     *
     * @param value encoded payload
     * @return decoded payload
     */
    private String decode(String value) {
        try {
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录凭证无法解析");
        }
    }

    /**
     * Value object returned after token creation.
     *
     * @param value raw token string
     * @param expiresIn remaining valid time in seconds
     * @param expiresAt exact expiration time
     */
    public record AuthToken(String value, Long expiresIn, LocalDateTime expiresAt) {
    }

    /**
     * Parsed token payload returned by the internal validation flow.
     *
     * @param userId authenticated user id
     * @param sessionId Redis-backed session id
     */
    private record TokenPayload(Long userId, String sessionId) {
    }
}
