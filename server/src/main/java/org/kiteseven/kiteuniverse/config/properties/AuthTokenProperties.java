package org.kiteseven.kiteuniverse.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds user token settings from the project configuration.
 */
@ConfigurationProperties(prefix = "kite-universe.jwt")
public class AuthTokenProperties {

    /**
     * Secret used to sign user tokens.
     */
    private String userSecretKeyString;

    /**
     * Token lifetime in milliseconds.
     */
    private Long userTtl = 7200000L;

    /**
     * Optional custom header name kept for compatibility.
     */
    private String userTokenName = "authentication";

    public String getUserSecretKeyString() {
        return userSecretKeyString;
    }

    public void setUserSecretKeyString(String userSecretKeyString) {
        this.userSecretKeyString = userSecretKeyString;
    }

    public Long getUserTtl() {
        return userTtl;
    }

    public void setUserTtl(Long userTtl) {
        this.userTtl = userTtl;
    }

    public String getUserTokenName() {
        return userTokenName;
    }

    public void setUserTokenName(String userTokenName) {
        this.userTokenName = userTokenName;
    }
}
