package org.kiteseven.kiteuniverse.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds auth security settings used by SMS rate limiting.
 */
@ConfigurationProperties(prefix = "kite-universe.auth")
public class AuthSecurityProperties {

    /**
     * Cooldown between two SMS send operations for the same phone and scene.
     */
    private long smsSendCooldownSeconds = 60L;

    /**
     * Daily SMS send limit for the same phone number.
     */
    private long smsDailyLimitPerPhone = 20L;

    /**
     * Sliding window length used for per-IP SMS limits.
     */
    private long smsIpWindowSeconds = 300L;

    /**
     * Maximum SMS send attempts allowed within the IP rate-limit window.
     */
    private long smsIpWindowLimit = 10L;

    public long getSmsSendCooldownSeconds() {
        return smsSendCooldownSeconds;
    }

    public void setSmsSendCooldownSeconds(long smsSendCooldownSeconds) {
        this.smsSendCooldownSeconds = smsSendCooldownSeconds;
    }

    public long getSmsDailyLimitPerPhone() {
        return smsDailyLimitPerPhone;
    }

    public void setSmsDailyLimitPerPhone(long smsDailyLimitPerPhone) {
        this.smsDailyLimitPerPhone = smsDailyLimitPerPhone;
    }

    public long getSmsIpWindowSeconds() {
        return smsIpWindowSeconds;
    }

    public void setSmsIpWindowSeconds(long smsIpWindowSeconds) {
        this.smsIpWindowSeconds = smsIpWindowSeconds;
    }

    public long getSmsIpWindowLimit() {
        return smsIpWindowLimit;
    }

    public void setSmsIpWindowLimit(long smsIpWindowLimit) {
        this.smsIpWindowLimit = smsIpWindowLimit;
    }
}
