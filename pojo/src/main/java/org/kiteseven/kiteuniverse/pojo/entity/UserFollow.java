package org.kiteseven.kiteuniverse.pojo.entity;

import org.kiteseven.kiteuniverse.pojo.base.BaseEntity;

import java.io.Serial;

/**
 * 用户关注关系实体类。
 */
public class UserFollow extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关注者用户编号。
     */
    private Long followerId;

    /**
     * 被关注者用户编号。
     */
    private Long followingId;

    /**
     * 关注状态，1 表示已关注。
     */
    private Integer status;

    public Long getFollowerId() {
        return followerId;
    }

    public void setFollowerId(Long followerId) {
        this.followerId = followerId;
    }

    public Long getFollowingId() {
        return followingId;
    }

    public void setFollowingId(Long followingId) {
        this.followingId = followingId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
