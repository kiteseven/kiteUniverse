package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.Notification;
import org.kiteseven.kiteuniverse.pojo.vo.notification.NotificationVO;

import java.util.List;

/**
 * 站内通知 MyBatis Mapper 接口。
 */
@Mapper
public interface NotificationMapper {

    /**
     * 插入一条通知记录。
     *
     * @param notification 通知实体
     */
    void insert(Notification notification);

    /**
     * 查询指定用户的通知列表，按创建时间倒序，最多返回 limit 条。
     *
     * @param recipientId 接收者编号
     * @param limit 最大返回数量
     * @return 通知视图列表
     */
    List<NotificationVO> selectByRecipientId(@Param("recipientId") Long recipientId,
                                             @Param("limit") int limit);

    /**
     * 查询指定用户的未读通知数量。
     *
     * @param recipientId 接收者编号
     * @return 未读数量
     */
    int countUnread(@Param("recipientId") Long recipientId);

    /**
     * 将指定用户的所有通知标记为已读。
     *
     * @param recipientId 接收者编号
     */
    void markAllRead(@Param("recipientId") Long recipientId);

    /**
     * 将指定通知标记为已读。
     *
     * @param id 通知编号
     * @param recipientId 接收者编号（安全校验）
     */
    void markRead(@Param("id") Long id, @Param("recipientId") Long recipientId);
}
