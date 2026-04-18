package org.kiteseven.kiteuniverse.pojo.dto.admin;

/**
 * 禁言用户请求 DTO。
 */
public class UserMuteDTO {

    /** 禁言时长（分钟），0 表示解除禁言。 */
    private int minutes;

    public int getMinutes() { return minutes; }
    public void setMinutes(int minutes) { this.minutes = minutes; }
}
