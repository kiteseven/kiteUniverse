package org.kiteseven.kiteuniverse.pojo.vo.auth;

import java.io.Serial;
import java.io.Serializable;

/**
 * Response returned after a verification code is created.
 */
public class SmsCodeSendVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Target mobile phone number.
     */
    private String phone;

    /**
     * Business scene of the code.
     */
    private String scene;

    /**
     * Verification code validity time in seconds.
     */
    private Long expireSeconds;

    /**
     * Indicates whether the backend is running in a debug-friendly environment.
     */
    private Boolean debugMode;

    /**
     * Debug code returned only in development for real integration testing.
     */
    private String debugCode;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public Long getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(Long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public Boolean getDebugMode() {
        return debugMode;
    }

    public void setDebugMode(Boolean debugMode) {
        this.debugMode = debugMode;
    }

    public String getDebugCode() {
        return debugCode;
    }

    public void setDebugCode(String debugCode) {
        this.debugCode = debugCode;
    }
}
