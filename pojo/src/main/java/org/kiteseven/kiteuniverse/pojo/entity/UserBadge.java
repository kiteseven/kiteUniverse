package org.kiteseven.kiteuniverse.pojo.entity;

import java.time.LocalDateTime;

/**
 * 用户已获得徽章记录。
 */
public class UserBadge {

    private Long id;
    private Long userId;
    /** 徽章类型标识，如 FIRST_CHECKIN / WEEK_STREAK 等 */
    private String badgeType;
    private LocalDateTime earnedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getBadgeType() { return badgeType; }
    public void setBadgeType(String badgeType) { this.badgeType = badgeType; }

    public LocalDateTime getEarnedAt() { return earnedAt; }
    public void setEarnedAt(LocalDateTime earnedAt) { this.earnedAt = earnedAt; }
}
