package org.kiteseven.kiteuniverse.pojo.dto.auth;

import java.io.Serial;
import java.io.Serializable;

/**
 * Request body for phone-based login.
 */
public class PhoneLoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Mobile phone number used as the login account.
     */
    private String phone;

    /**
     * Verification code sent to the phone.
     */
    private String code;

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
}
