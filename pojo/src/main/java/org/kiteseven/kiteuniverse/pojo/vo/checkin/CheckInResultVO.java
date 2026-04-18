package org.kiteseven.kiteuniverse.pojo.vo.checkin;

import java.util.List;
import org.kiteseven.kiteuniverse.pojo.vo.checkin.UserBadgeVO;

/**
 * 签到操作结果视图对象。
 */
public class CheckInResultVO {

    /** 本次签到获得积分 */
    private Integer pointsEarned;
    /** 连续签到天数 */
    private Integer consecutiveDays;
    /** 当前总积分 */
    private Integer totalPoints;
    /** 当前等级 */
    private Integer level;
    /** 等级名称 */
    private String levelName;
    /** 是否升级 */
    private boolean leveledUp;
    /** 本次获得的新徽章列表 */
    private List<UserBadgeVO> newBadges;

    public Integer getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(Integer pointsEarned) { this.pointsEarned = pointsEarned; }

    public Integer getConsecutiveDays() { return consecutiveDays; }
    public void setConsecutiveDays(Integer consecutiveDays) { this.consecutiveDays = consecutiveDays; }

    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }

    public boolean isLeveledUp() { return leveledUp; }
    public void setLeveledUp(boolean leveledUp) { this.leveledUp = leveledUp; }

    public List<UserBadgeVO> getNewBadges() { return newBadges; }
    public void setNewBadges(List<UserBadgeVO> newBadges) { this.newBadges = newBadges; }
}
