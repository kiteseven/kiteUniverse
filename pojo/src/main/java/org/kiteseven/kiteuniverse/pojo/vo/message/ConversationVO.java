package org.kiteseven.kiteuniverse.pojo.vo.message;

import java.time.LocalDateTime;

/**
 * 私信会话列表视图对象。
 */
public class ConversationVO {

    private Long otherUserId;
    private String otherUserName;
    private String otherUserAvatar;
    /** 最后一条消息内容摘要（截断至 60 字符） */
    private String lastMessageContent;
    /** 最后一条消息是否由当前用户发出 */
    private boolean lastMessageByMe;
    /** 未读消息数 */
    private Integer unreadCount;
    private LocalDateTime lastMessageTime;

    public Long getOtherUserId() { return otherUserId; }
    public void setOtherUserId(Long otherUserId) { this.otherUserId = otherUserId; }

    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }

    public String getOtherUserAvatar() { return otherUserAvatar; }
    public void setOtherUserAvatar(String otherUserAvatar) { this.otherUserAvatar = otherUserAvatar; }

    public String getLastMessageContent() { return lastMessageContent; }
    public void setLastMessageContent(String lastMessageContent) { this.lastMessageContent = lastMessageContent; }

    public boolean isLastMessageByMe() { return lastMessageByMe; }
    public void setLastMessageByMe(boolean lastMessageByMe) { this.lastMessageByMe = lastMessageByMe; }

    public Integer getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Integer unreadCount) { this.unreadCount = unreadCount; }

    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }
}
