package org.kiteseven.kiteuniverse.pojo.vo.game;

import java.time.LocalDateTime;

/**
 * 游戏数据快照视图对象。
 */
public class GameStatsVO {

    private Long id;
    private Integer actionPoint;
    private Integer maxActionPoint;
    private Integer voidShards;
    private Integer accountLevel;
    private Integer totalRuns;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
