package org.kiteseven.kiteuniverse.pojo.vo.game;

import java.time.LocalDateTime;

/**
 * 跑图记录视图对象。
 */
public class GameCharacterVO {

    private Long id;
    private String classId;
    private String className;
    private Integer ascensionLevel;
    private Integer actReached;
    private Integer floorReached;
    private Integer score;
    private String keyRelic;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Integer getAscensionLevel() { return ascensionLevel; }
    public void setAscensionLevel(Integer ascensionLevel) { this.ascensionLevel = ascensionLevel; }

    public Integer getActReached() { return actReached; }
    public void setActReached(Integer actReached) { this.actReached = actReached; }

    public Integer getFloorReached() { return floorReached; }
    public void setFloorReached(Integer floorReached) { this.floorReached = floorReached; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getKeyRelic() { return keyRelic; }
    public void setKeyRelic(String keyRelic) { this.keyRelic = keyRelic; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
