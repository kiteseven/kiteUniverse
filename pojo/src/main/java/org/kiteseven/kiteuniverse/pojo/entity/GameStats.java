package org.kiteseven.kiteuniverse.pojo.entity;

import java.time.LocalDateTime;

/**
 * 玩家游戏数据快照（行动力、虚空碎片、跑图次数等）。
 */
public class GameStats {

    private Long id;
    private Long userId;
    private Integer actionPoint;
    private Integer maxActionPoint;
    private Integer voidShards;
    private Integer accountLevel;
    private Integer totalRuns;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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
