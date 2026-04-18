package org.kiteseven.kiteuniverse.pojo.dto.game;

/**
 * 添加跑图记录请求对象。
 */
public class GameCharacterAddDTO {

    private String classId;
    private String className;
    private Integer ascensionLevel;
    private Integer actReached;
    private Integer floorReached;
    private Integer score;
    private String keyRelic;

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
}
