package org.kiteseven.kiteuniverse.support.notification;

import org.kiteseven.kiteuniverse.mapper.NotificationMapper;
import org.kiteseven.kiteuniverse.mapper.UserMapper;
import org.kiteseven.kiteuniverse.pojo.entity.Notification;
import org.kiteseven.kiteuniverse.pojo.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Turns async notification commands into database writes and WebSocket pushes.
 */
@Service
public class NotificationCommandProcessor {

    private static final Logger log = LoggerFactory.getLogger(NotificationCommandProcessor.class);

    private static final String TYPE_COMMENT = "COMMENT";
    private static final String TYPE_POST_LIKE = "POST_LIKE";
    private static final String TYPE_COMMENT_LIKE = "COMMENT_LIKE";
    private static final String TYPE_FOLLOW = "FOLLOW";
    private static final String TYPE_ANNOUNCEMENT = "ANNOUNCEMENT";

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider;

    public NotificationCommandProcessor(NotificationMapper notificationMapper,
                                        UserMapper userMapper,
                                        ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider) {
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
        this.messagingTemplateProvider = messagingTemplateProvider;
    }

    public void process(NotificationCommand notificationCommand) {
        if (notificationCommand == null || notificationCommand.getType() == null) {
            return;
        }

        switch (notificationCommand.getType()) {
            case COMMENT -> processComment(notificationCommand);
            case POST_LIKE -> processPostLike(notificationCommand);
            case COMMENT_LIKE -> processCommentLike(notificationCommand);
            case FOLLOW -> processFollow(notificationCommand);
            case ANNOUNCEMENT -> processAnnouncement(notificationCommand);
            default -> log.warn("Unsupported notification command type: {}", notificationCommand.getType());
        }
    }

    private void processComment(NotificationCommand notificationCommand) {
        Long senderId = notificationCommand.getSenderId();
        Long recipientId = notificationCommand.getRecipientId();
        if (senderId == null || recipientId == null || senderId.equals(recipientId)) {
            return;
        }

        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setSenderId(senderId);
        notification.setType(TYPE_COMMENT);
        notification.setPostId(notificationCommand.getPostId());
        notification.setCommentId(notificationCommand.getCommentId());
        notification.setContent(resolveName(senderId) + " 评论了你的帖子：" + truncate(notificationCommand.getContent(), 50));
        notification.setIsRead(0);
        notificationMapper.insert(notification);
        pushUnreadCount(recipientId);
    }

    private void processPostLike(NotificationCommand notificationCommand) {
        Long senderId = notificationCommand.getSenderId();
        Long recipientId = notificationCommand.getRecipientId();
        if (senderId == null || recipientId == null || senderId.equals(recipientId)) {
            return;
        }

        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setSenderId(senderId);
        notification.setType(TYPE_POST_LIKE);
        notification.setPostId(notificationCommand.getPostId());
        notification.setContent(resolveName(senderId) + " 点赞了你的帖子");
        notification.setIsRead(0);
        notificationMapper.insert(notification);
        pushUnreadCount(recipientId);
    }

    private void processCommentLike(NotificationCommand notificationCommand) {
        Long senderId = notificationCommand.getSenderId();
        Long recipientId = notificationCommand.getRecipientId();
        if (senderId == null || recipientId == null || senderId.equals(recipientId)) {
            return;
        }

        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setSenderId(senderId);
        notification.setType(TYPE_COMMENT_LIKE);
        notification.setPostId(notificationCommand.getPostId());
        notification.setCommentId(notificationCommand.getCommentId());
        notification.setContent(resolveName(senderId) + " 点赞了你的评论");
        notification.setIsRead(0);
        notificationMapper.insert(notification);
        pushUnreadCount(recipientId);
    }

    private void processFollow(NotificationCommand notificationCommand) {
        Long senderId = notificationCommand.getSenderId();
        Long recipientId = notificationCommand.getRecipientId();
        if (senderId == null || recipientId == null || senderId.equals(recipientId)) {
            return;
        }

        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setSenderId(senderId);
        notification.setType(TYPE_FOLLOW);
        notification.setContent(resolveName(senderId) + " 关注了你");
        notification.setIsRead(0);
        notificationMapper.insert(notification);
        pushUnreadCount(recipientId);
    }

    private void processAnnouncement(NotificationCommand notificationCommand) {
        if (!StringUtils.hasText(notificationCommand.getContent())) {
            return;
        }

        String announcementContent = notificationCommand.getContent().trim();
        List<User> allUsers = userMapper.selectAll();
        for (User user : allUsers) {
            Notification notification = new Notification();
            notification.setRecipientId(user.getId());
            notification.setSenderId(null);
            notification.setType(TYPE_ANNOUNCEMENT);
            notification.setContent(announcementContent);
            notification.setIsRead(0);
            notificationMapper.insert(notification);
        }
        pushSystemAnnouncement(announcementContent);
    }

    private void pushUnreadCount(Long recipientId) {
        SimpMessagingTemplate messagingTemplate = messagingTemplateProvider.getIfAvailable();
        if (messagingTemplate == null) {
            return;
        }
        try {
            int unread = notificationMapper.countUnread(recipientId);
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(recipientId),
                    "/queue/notifications",
                    Map.of("unreadCount", unread)
            );
        } catch (Exception exception) {
            log.debug("Failed to push notification unread count via WebSocket", exception);
        }
    }

    private void pushSystemAnnouncement(String content) {
        SimpMessagingTemplate messagingTemplate = messagingTemplateProvider.getIfAvailable();
        if (messagingTemplate == null) {
            return;
        }
        try {
            messagingTemplate.convertAndSend(
                    "/topic/system",
                    (Object) Map.of("type", "announcement", "content", content)
            );
        } catch (Exception exception) {
            log.debug("Failed to push system announcement via WebSocket", exception);
        }
    }

    private String resolveName(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return "某位用户";
        }
        return StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
    }

    private String truncate(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String trimmed = text.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) + "..." : trimmed;
    }
}
