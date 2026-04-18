package org.kiteseven.kiteuniverse.support.notification;

/**
 * One async notification command delivered through the message queue.
 */
public class NotificationCommand {

    public enum Type {
        COMMENT,
        POST_LIKE,
        COMMENT_LIKE,
        FOLLOW,
        ANNOUNCEMENT
    }

    private Type type;
    private Long senderId;
    private Long recipientId;
    private Long postId;
    private Long commentId;
    private String content;

    public static NotificationCommand comment(Long senderId,
                                              Long recipientId,
                                              Long postId,
                                              Long commentId,
                                              String content) {
        NotificationCommand command = new NotificationCommand();
        command.setType(Type.COMMENT);
        command.setSenderId(senderId);
        command.setRecipientId(recipientId);
        command.setPostId(postId);
        command.setCommentId(commentId);
        command.setContent(content);
        return command;
    }

    public static NotificationCommand postLike(Long senderId, Long recipientId, Long postId) {
        NotificationCommand command = new NotificationCommand();
        command.setType(Type.POST_LIKE);
        command.setSenderId(senderId);
        command.setRecipientId(recipientId);
        command.setPostId(postId);
        return command;
    }

    public static NotificationCommand commentLike(Long senderId,
                                                  Long recipientId,
                                                  Long postId,
                                                  Long commentId) {
        NotificationCommand command = new NotificationCommand();
        command.setType(Type.COMMENT_LIKE);
        command.setSenderId(senderId);
        command.setRecipientId(recipientId);
        command.setPostId(postId);
        command.setCommentId(commentId);
        return command;
    }

    public static NotificationCommand follow(Long senderId, Long recipientId) {
        NotificationCommand command = new NotificationCommand();
        command.setType(Type.FOLLOW);
        command.setSenderId(senderId);
        command.setRecipientId(recipientId);
        return command;
    }

    public static NotificationCommand announcement(String content) {
        NotificationCommand command = new NotificationCommand();
        command.setType(Type.ANNOUNCEMENT);
        command.setContent(content);
        return command;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
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
}
