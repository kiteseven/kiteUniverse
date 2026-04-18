package org.kiteseven.kiteuniverse.pojo.vo.checkin;

/**
 * 今日签到状态视图对象。
 */
public class CheckInStatusVO {

    /** 今日是否已签到 */
    private boolean checkedInToday;
    /** 连续签到天数 */
    private Integer consecutiveDays;
    /** 当前积分 */
    private Integer points;
    /** 当前等级 */
    private Integer level;
    /** 等级名称 */
    private String levelName;
    /** 下一级所需积分（已满级则为 -1）*/
    private Integer nextLevelPoints;

    public boolean isCheckedInToday() { return checkedInToday; }
    public void setCheckedInToday(boolean checkedInToday) { this.checkedInToday = checkedInToday; }

    public Integer getConsecutiveDays() { return consecutiveDays; }
    public void setConsecutiveDays(Integer consecutiveDays) { this.consecutiveDays = consecutiveDays; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }

    public Integer getNextLevelPoints() { return nextLevelPoints; }
    public void setNextLevelPoints(Integer nextLevelPoints) { this.nextLevelPoints = nextLevelPoints; }
}
