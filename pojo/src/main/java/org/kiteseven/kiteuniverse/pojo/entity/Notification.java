package org.kiteseven.kiteuniverse.pojo.entity;

import org.kiteseven.kiteuniverse.pojo.base.BaseEntity;

import java.io.Serial;

/**
 * 站内通知实体类。
 */
public class Notification extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 接收通知的用户编号。
     */
    private Long recipientId;

    /**
     * 触发通知的用户编号，系统公告时为 null。
     */
    private Long senderId;

    /**
     * 通知类型：COMMENT、POST_LIKE、COMMENT_LIKE、FOLLOW、ANNOUNCEMENT。
     */
    private String type;

    /**
     * 关联的帖子编号，与通知类型相关。
     */
    private Long postId;

    /**
     * 关联的评论编号，与通知类型相关。
     */
    private Long commentId;

    /**
     * 通知内容摘要，用于展示。
     */
    private String content;

    /**
     * 是否已读，0 未读，1 已读。
     */
    private Integer isRead;

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }
}
