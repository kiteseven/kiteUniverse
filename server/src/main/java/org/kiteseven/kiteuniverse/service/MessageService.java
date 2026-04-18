package org.kiteseven.kiteuniverse.service;

import org.kiteseven.kiteuniverse.pojo.dto.message.SendMessageDTO;
import org.kiteseven.kiteuniverse.pojo.vo.message.ConversationVO;
import org.kiteseven.kiteuniverse.pojo.vo.message.MessageVO;

import java.util.List;

/**
 * 私信服务接口。
 */
public interface MessageService {

    /** 发送私信。 */
    MessageVO sendMessage(Long senderId, SendMessageDTO dto);

    /** 查询与指定用户的聊天历史（升序分页）。 */
    List<MessageVO> getMessages(Long userId, Long otherId, int limit, int offset);

    /** 将对方发来的消息全部标为已读。 */
    void markAsRead(Long recipientId, Long senderId);

    /** 获取当前用户的未读私信总数。 */
    int countUnread(Long userId);

    /** 获取当前用户的会话列表。 */
    List<ConversationVO> getConversations(Long userId);
}
