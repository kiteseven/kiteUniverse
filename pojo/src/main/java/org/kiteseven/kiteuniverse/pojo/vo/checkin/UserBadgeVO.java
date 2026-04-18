package org.kiteseven.kiteuniverse.pojo.vo.checkin;

import java.time.LocalDateTime;

/**
 * 用户徽章视图对象。
 */
public class UserBadgeVO {

    private String badgeType;
    private String name;
    private String description;
    private String icon;
    private LocalDateTime earnedAt;

    public String getBadgeType() { return badgeType; }
    public void setBadgeType(String badgeType) { this.badgeType = badgeType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public LocalDateTime getEarnedAt() { return earnedAt; }
    public void setEarnedAt(LocalDateTime earnedAt) { this.earnedAt = earnedAt; }
}
