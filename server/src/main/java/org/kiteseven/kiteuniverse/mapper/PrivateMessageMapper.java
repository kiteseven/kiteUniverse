package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.PrivateMessage;
import org.kiteseven.kiteuniverse.pojo.vo.message.ConversationVO;
import org.kiteseven.kiteuniverse.pojo.vo.message.MessageVO;

import java.util.List;

/**
 * 私信消息数据访问接口。
 */
@Mapper
public interface PrivateMessageMapper {

    /** 发送一条消息。 */
    int insert(PrivateMessage message);

    /**
     * 查询两用户之间的消息历史（时间升序），支持分页。
     *
     * @param userId    当前用户
     * @param otherId   对方用户
     * @param limit     最多返回条数
     * @param offset    跳过条数
     */
    List<MessageVO> selectConversationMessages(
            @Param("userId") Long userId,
            @Param("otherId") Long otherId,
            @Param("limit") int limit,
            @Param("offset") int offset);

    /** 标记某用户发给 userId 的所有消息为已读。 */
    int markAsRead(@Param("recipientId") Long recipientId, @Param("senderId") Long senderId);

    /** 查询 userId 的未读消息总数。 */
    int countUnread(@Param("userId") Long userId);

    /**
     * 查询 userId 的会话列表（按最后消息时间倒序）。
     * 每条会话包含对方用户信息、最后消息摘要、未读数。
     */
    List<ConversationVO> selectConversations(@Param("userId") Long userId);
}
