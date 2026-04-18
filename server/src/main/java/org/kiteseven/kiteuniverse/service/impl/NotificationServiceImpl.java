package org.kiteseven.kiteuniverse.service.impl;

import org.kiteseven.kiteuniverse.mapper.NotificationMapper;
import org.kiteseven.kiteuniverse.pojo.vo.notification.NotificationVO;
import org.kiteseven.kiteuniverse.pojo.vo.notification.UnreadCountVO;
import org.kiteseven.kiteuniverse.service.NotificationService;
import org.kiteseven.kiteuniverse.support.notification.NotificationCommand;
import org.kiteseven.kiteuniverse.support.notification.NotificationCommandPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Notification query service plus async notification command publisher.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final NotificationCommandPublisher notificationCommandPublisher;

    public NotificationServiceImpl(NotificationMapper notificationMapper,
                                   NotificationCommandPublisher notificationCommandPublisher) {
        this.notificationMapper = notificationMapper;
        this.notificationCommandPublisher = notificationCommandPublisher;
    }

    @Override
    public List<NotificationVO> listNotifications(Long recipientId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return notificationMapper.selectByRecipientId(recipientId, safeLimit);
    }

    @Override
    public UnreadCountVO getUnreadCount(Long recipientId) {
        return new UnreadCountVO(notificationMapper.countUnread(recipientId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(Long recipientId) {
        notificationMapper.markAllRead(recipientId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long notificationId, Long recipientId) {
        notificationMapper.markRead(notificationId, recipientId);
    }

    @Override
    public void publishAnnouncement(String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }
        notificationCommandPublisher.publishAfterCommit(NotificationCommand.announcement(content.trim()));
    }

    @Override
    public void createCommentNotification(Long senderId, Long postId, Long postAuthorId,
                                          Long commentId, String commentContent) {
        if (senderId == null || postAuthorId == null || senderId.equals(postAuthorId)) {
            return;
        }
        notificationCommandPublisher.publishAfterCommit(
                NotificationCommand.comment(senderId, postAuthorId, postId, commentId, commentContent)
        );
    }

    @Override
    public void createPostLikeNotification(Long senderId, Long postId, Long postAuthorId) {
        if (senderId == null || postAuthorId == null || senderId.equals(postAuthorId)) {
            return;
        }
        notificationCommandPublisher.publishAfterCommit(
                NotificationCommand.postLike(senderId, postAuthorId, postId)
        );
    }

    @Override
    public void createCommentLikeNotification(Long senderId, Long commentId, Long commentAuthorId, Long postId) {
        if (senderId == null || commentAuthorId == null || senderId.equals(commentAuthorId)) {
            return;
        }
        notificationCommandPublisher.publishAfterCommit(
                NotificationCommand.commentLike(senderId, commentAuthorId, postId, commentId)
        );
    }

    @Override
    public void createFollowNotification(Long senderId, Long targetUserId) {
        if (senderId == null || targetUserId == null || senderId.equals(targetUserId)) {
            return;
        }
        notificationCommandPublisher.publishAfterCommit(
                NotificationCommand.follow(senderId, targetUserId)
        );
    }
}
