package org.kiteseven.kiteuniverse.service;

import org.kiteseven.kiteuniverse.pojo.vo.notification.NotificationVO;
import org.kiteseven.kiteuniverse.pojo.vo.notification.UnreadCountVO;

import java.util.List;

/**
 * 站内通知服务接口。
 */
public interface NotificationService {

    /**
     * 查询指定用户的通知列表。
     *
     * @param recipientId 接收者编号
     * @param limit 最大返回数量
     * @return 通知视图列表
     */
    List<NotificationVO> listNotifications(Long recipientId, int limit);

    /**
     * 查询指定用户的未读通知数量。
     *
     * @param recipientId 接收者编号
     * @return 未读数量视图
     */
    UnreadCountVO getUnreadCount(Long recipientId);

    /**
     * 将指定用户所有通知标记为已读。
     *
     * @param recipientId 接收者编号
     */
    void markAllRead(Long recipientId);

    /**
     * 将指定通知标记为已读。
     *
     * @param notificationId 通知编号
     * @param recipientId 接收者编号
     */
    void markRead(Long notificationId, Long recipientId);

    /**
     * 发布系统公告通知（发送给所有用户）。
     * 仅管理员调用。
     *
     * @param content 公告内容
     */
    void publishAnnouncement(String content);

    /**
     * 创建评论通知：通知帖子作者有新评论。
     *
     * @param senderId 评论者编号
     * @param postId 帖子编号
     * @param postAuthorId 帖子作者编号
     * @param commentId 评论编号
     * @param commentContent 评论内容摘要
     */
    void createCommentNotification(Long senderId, Long postId, Long postAuthorId,
                                   Long commentId, String commentContent);

    /**
     * 创建帖子点赞通知：通知帖子作者有人点赞。
     *
     * @param senderId 点赞者编号
     * @param postId 帖子编号
     * @param postAuthorId 帖子作者编号
     */
    void createPostLikeNotification(Long senderId, Long postId, Long postAuthorId);

    /**
     * 创建评论点赞通知：通知评论作者有人点赞。
     *
     * @param senderId 点赞者编号
     * @param commentId 评论编号
     * @param commentAuthorId 评论作者编号
     * @param postId 所属帖子编号
     */
    void createCommentLikeNotification(Long senderId, Long commentId, Long commentAuthorId, Long postId);

    /**
     * 创建关注通知：通知被关注者。
     *
     * @param senderId 关注者编号
     * @param targetUserId 被关注者编号
     */
    void createFollowNotification(Long senderId, Long targetUserId);
}
