package org.kiteseven.kiteuniverse.pojo.vo.notification;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知列表项视图对象。
 */
public class NotificationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 通知编号。
     */
    private Long id;

    /**
     * 通知类型：COMMENT、POST_LIKE、COMMENT_LIKE、FOLLOW、ANNOUNCEMENT。
     */
    private String type;

    /**
     * 触发通知的用户编号。
     */
    private Long senderId;

    /**
     * 触发通知的用户名。
     */
    private String senderName;

    /**
     * 触发通知的用户头像。
     */
    private String senderAvatar;

    /**
     * 关联帖子编号。
     */
    private Long postId;

    /**
     * 关联帖子标题。
     */
    private String postTitle;

    /**
     * 关联评论编号。
     */
    private Long commentId;

    /**
     * 通知内容摘要。
     */
    private String content;

    /**
     * 是否已读。
     */
    private Boolean isRead;

    /**
     * 通知创建时间。
     */
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
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

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
