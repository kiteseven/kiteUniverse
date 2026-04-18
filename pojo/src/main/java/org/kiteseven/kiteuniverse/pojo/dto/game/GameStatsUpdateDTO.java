package org.kiteseven.kiteuniverse.pojo.dto.game;

/**
 * 游戏数据更新请求对象。
 */
public class GameStatsUpdateDTO {

    private Integer actionPoint;
    private Integer maxActionPoint;
    private Integer voidShards;
    private Integer accountLevel;
    private Integer totalRuns;

    public Integer getActionPoint() { return actionPoint; }
    public void setActionPoint(Integer actionPoint) { this.actionPoint = actionPoint; }

    public Integer getMaxActionPoint() { return maxActionPoint; }
    public void setMaxActionPoint(Integer maxActionPoint) { this.maxActionPoint = maxActionPoint; }

    public Integer getVoidShards() { return voidShards; }
    public void setVoidShards(Integer voidShards) { this.voidShards = voidShards; }

    public Integer getAccountLevel() { return accountLevel; }
    public void setAccountLevel(Integer accountLevel) { this.accountLevel = accountLevel; }

    public Integer getTotalRuns() { return totalRuns; }
    public void setTotalRuns(Integer totalRuns) { this.totalRuns = totalRuns; }
}
