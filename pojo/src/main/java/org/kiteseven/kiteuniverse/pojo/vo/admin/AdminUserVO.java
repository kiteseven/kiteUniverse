package org.kiteseven.kiteuniverse.pojo.vo.admin;

import java.time.LocalDateTime;

/**
 * 管理后台用户列表行 VO。
 */
public class AdminUserVO {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String avatar;
    private Integer status;
    private String role;
    private LocalDateTime muteUntil;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getMuteUntil() { return muteUntil; }
    public void setMuteUntil(LocalDateTime muteUntil) { this.muteUntil = muteUntil; }

    public LocalDateTime getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(LocalDateTime lastLoginTime) { this.lastLoginTime = lastLoginTime; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
