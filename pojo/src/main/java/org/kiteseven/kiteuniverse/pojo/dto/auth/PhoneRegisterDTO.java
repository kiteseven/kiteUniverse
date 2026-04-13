package org.kiteseven.kiteuniverse.pojo.dto.auth;

import java.io.Serial;
import java.io.Serializable;

/**
 * Request body for phone-based registration.
 */
public class PhoneRegisterDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Mobile phone number used for registration.
     */
    private String phone;

    /**
     * Verification code used to confirm the phone number.
     */
    private String code;

    /**
     * Optional nickname shown in the community.
     */
    private String nickname;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
