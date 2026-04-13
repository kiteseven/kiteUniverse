package org.kiteseven.kiteuniverse.pojo.vo.auth;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Authentication response returned to the frontend after login or registration.
 */
public class AuthResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Signed token used for subsequent requests.
     */
    private String token;

    /**
     * Token type used by the client.
     */
    private String tokenType;

    /**
     * Remaining valid time in seconds.
     */
    private Long expiresIn;

    /**
     * Exact token expiration time.
     */
    private LocalDateTime expiresAt;

    /**
     * Authenticated user summary.
     */
    private AuthUserVO user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public AuthUserVO getUser() {
        return user;
    }

    public void setUser(AuthUserVO user) {
        this.user = user;
    }
}
