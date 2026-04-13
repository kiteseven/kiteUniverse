package org.kiteseven.kiteuniverse.pojo.dto.auth;

import java.io.Serial;
import java.io.Serializable;

/**
 * Request body for sending a phone verification code.
 */
public class SmsCodeSendDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Target mobile phone number.
     */
    private String phone;

    /**
     * Business scene, for example login or register.
     */
    private String scene;

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
}
