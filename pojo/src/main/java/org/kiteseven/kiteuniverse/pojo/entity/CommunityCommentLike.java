package org.kiteseven.kiteuniverse.pojo.entity;

import org.kiteseven.kiteuniverse.pojo.base.BaseEntity;

import java.io.Serial;

/**
 * 评论点赞关系实体类。
 */
public class CommunityCommentLike extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论编号。
     */
    private Long commentId;

    /**
     * 点赞用户编号。
     */
    private Long userId;

    /**
     * 点赞状态，1 表示已点赞。
     */
    private Integer status;

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
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
