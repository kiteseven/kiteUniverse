package org.kiteseven.kiteuniverse.pojo.entity;

import org.kiteseven.kiteuniverse.pojo.base.BaseEntity;

import java.io.Serial;

/**
 * 帖子点赞关系实体类。
 */
public class CommunityPostLike extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 帖子编号。
     */
    private Long postId;

    /**
     * 点赞用户编号。
     */
    private Long userId;

    /**
     * 点赞状态，1 表示已点赞。
     */
    private Integer status;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
