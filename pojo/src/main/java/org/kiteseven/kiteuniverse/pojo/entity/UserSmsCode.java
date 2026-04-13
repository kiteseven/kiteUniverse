package org.kiteseven.kiteuniverse.pojo.entity;

import org.kiteseven.kiteuniverse.pojo.base.BaseEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * Stores phone verification codes used during login and registration.
 */
public class UserSmsCode extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Mobile phone number that receives the code.
     */
    private String phone;

    /**
     * Business scene of the code, such as login or register.
     */
    private String bizType;

    /**
     * Six-digit verification code.
     */
    private String code;

    /**
     * Code status: 0 unused, 1 used, 2 expired.
     */
    private Integer status;

    /**
     * Time when the code becomes invalid.
     */
    private LocalDateTime expiresAt;

    /**
     * Time when the code is consumed.
     */
    private LocalDateTime usedAt;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
}
