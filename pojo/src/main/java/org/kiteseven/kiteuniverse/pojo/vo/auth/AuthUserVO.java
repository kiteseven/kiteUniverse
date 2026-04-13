package org.kiteseven.kiteuniverse.pojo.vo.auth;

import java.io.Serial;
import java.io.Serializable;

/**
 * Lightweight user information returned after authentication.
 */
public class AuthUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * User identifier.
     */
    private Long id;

    /**
     * Internal username generated for the account.
     */
    private String username;

    /**
     * Nickname shown in the UI.
     */
    private String nickname;

    /**
     * Bound mobile phone number.
     */
    private String phone;

    /**
     * User avatar URL.
     */
    private String avatar;

    /**
     * User status.
     */
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
