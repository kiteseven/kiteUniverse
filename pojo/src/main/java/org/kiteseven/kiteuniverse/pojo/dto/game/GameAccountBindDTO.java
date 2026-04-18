package org.kiteseven.kiteuniverse.pojo.dto.game;

/**
 * 游戏账号绑定请求对象。
 */
public class GameAccountBindDTO {

    private String gameUid;
    private String serverName;
    private String inGameName;
    private Integer accountLevel;

    public String getGameUid() { return gameUid; }
    public void setGameUid(String gameUid) { this.gameUid = gameUid; }

    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }

    public String getInGameName() { return inGameName; }
    public void setInGameName(String inGameName) { this.inGameName = inGameName; }

    public Integer getAccountLevel() { return accountLevel; }
    public void setAccountLevel(Integer accountLevel) { this.accountLevel = accountLevel; }
}
