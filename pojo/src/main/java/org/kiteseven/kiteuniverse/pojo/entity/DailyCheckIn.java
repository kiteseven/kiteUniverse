package org.kiteseven.kiteuniverse.pojo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日签到记录。
 */
public class DailyCheckIn {

    private Long id;
    private Long userId;
    /** 签到日期 */
    private LocalDate checkInDate;
    /** 本次签到奖励积分 */
    private Integer pointsEarned;
    /** 签到时的连续天数 */
    private Integer consecutiveDays;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public Integer getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(Integer pointsEarned) { this.pointsEarned = pointsEarned; }

    public Integer getConsecutiveDays() { return consecutiveDays; }
    public void setConsecutiveDays(Integer consecutiveDays) { this.consecutiveDays = consecutiveDays; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
