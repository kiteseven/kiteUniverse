package org.kiteseven.kiteuniverse.pojo.entity;

import java.time.LocalDateTime;

/**
 * 游戏账号绑定记录。
 */
public class GameAccount {

    private Long id;
    private Long userId;
    private String gameUid;
    private String serverName;
    private String inGameName;
    private Integer accountLevel;
    private Integer status;
    private LocalDateTime bindTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getGameUid() { return gameUid; }
    public void setGameUid(String gameUid) { this.gameUid = gameUid; }

    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }

    public String getInGameName() { return inGameName; }
    public void setInGameName(String inGameName) { this.inGameName = inGameName; }

    public Integer getAccountLevel() { return accountLevel; }
    public void setAccountLevel(Integer accountLevel) { this.accountLevel = accountLevel; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getBindTime() { return bindTime; }
    public void setBindTime(LocalDateTime bindTime) { this.bindTime = bindTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
