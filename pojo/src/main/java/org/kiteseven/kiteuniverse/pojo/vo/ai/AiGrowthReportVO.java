package org.kiteseven.kiteuniverse.pojo.vo.ai;

/**
 * AI 生成的玩家成长报告。
 */
public class AiGrowthReportVO {

    /**
     * 用户编号。
     */
    private Long userId;

    /**
     * 当前社区等级名称。
     */
    private String levelName;

    /**
     * 当前积分。
     */
    private int points;

    /**
     * AI 生成的个性化发展报告正文（Markdown 格式）。
     */
    private String report;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }
}
