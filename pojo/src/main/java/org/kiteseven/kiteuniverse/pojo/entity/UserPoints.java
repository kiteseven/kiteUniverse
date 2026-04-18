package org.kiteseven.kiteuniverse.pojo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户积分与等级快照。
 */
public class UserPoints {

    private Long id;
    private Long userId;
    /** 当前积分 */
    private Integer points;
    /** 当前等级 1–6 */
    private Integer level;
    /** 累计获得积分 */
    private Integer totalPointsEarned;
    /** 连续签到天数 */
    private Integer consecutiveDays;
    /** 最后一次签到日期 */
    private LocalDate lastCheckInDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Integer getTotalPointsEarned() { return totalPointsEarned; }
    public void setTotalPointsEarned(Integer totalPointsEarned) { this.totalPointsEarned = totalPointsEarned; }

    public Integer getConsecutiveDays() { return consecutiveDays; }
    public void setConsecutiveDays(Integer consecutiveDays) { this.consecutiveDays = consecutiveDays; }

    public LocalDate getLastCheckInDate() { return lastCheckInDate; }
    public void setLastCheckInDate(LocalDate lastCheckInDate) { this.lastCheckInDate = lastCheckInDate; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
