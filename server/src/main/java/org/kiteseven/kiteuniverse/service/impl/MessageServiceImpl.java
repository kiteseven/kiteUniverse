package org.kiteseven.kiteuniverse.service.impl;

import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.mapper.PrivateMessageMapper;
import org.kiteseven.kiteuniverse.mapper.UserMapper;
import org.kiteseven.kiteuniverse.pojo.dto.message.SendMessageDTO;
import org.kiteseven.kiteuniverse.pojo.entity.PrivateMessage;
import org.kiteseven.kiteuniverse.pojo.entity.User;
import org.kiteseven.kiteuniverse.pojo.vo.message.ConversationVO;
import org.kiteseven.kiteuniverse.pojo.vo.message.MessageVO;
import org.kiteseven.kiteuniverse.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 私信服务实现。
 * 在消息发送后，通过 WebSocket 实时推送给收信方。
 */
@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final PrivateMessageMapper privateMessageMapper;
    private final UserMapper userMapper;

    /**
     * 使用 @Lazy 避免 WebSocket 基础设施循环依赖问题。
     */
    @Lazy
    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    public MessageServiceImpl(PrivateMessageMapper privateMessageMapper, UserMapper userMapper) {
        this.privateMessageMapper = privateMessageMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendMessage(Long senderId, SendMessageDTO dto) {
        if (dto == null || dto.getRecipientId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "收信人不能为空");
        }
        if (!StringUtils.hasText(dto.getContent())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "消息内容不能为空");
        }
        if (dto.getContent().trim().length() > 1000) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "消息内容不能超过 1000 字符");
        }
        if (senderId.equals(dto.getRecipientId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能给自己发送私信");
        }

        User recipient = userMapper.selectById(dto.getRecipientId());
        if (recipient == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "对方用户不存在");
        }
        User sender = userMapper.selectById(senderId);

        PrivateMessage msg = new PrivateMessage();
        msg.setSenderId(senderId);
        msg.setRecipientId(dto.getRecipientId());
        msg.setContent(dto.getContent().trim());
        msg.setCreateTime(LocalDateTime.now());
        privateMessageMapper.insert(msg);

        MessageVO vo = new MessageVO();
        vo.setId(msg.getId());
        vo.setSenderId(senderId);
        vo.setSenderName(sender != null
                ? (StringUtils.hasText(sender.getNickname()) ? sender.getNickname() : sender.getUsername())
                : String.valueOf(senderId));
        vo.setSenderAvatar(sender != null ? sender.getAvatar() : null);
        vo.setRecipientId(dto.getRecipientId());
        vo.setContent(msg.getContent());
        vo.setRead(false);
        vo.setCreateTime(msg.getCreateTime());

        // 实时推送给收信方
        pushMessageToRecipient(dto.getRecipientId(), vo);

        return vo;
    }

    @Override
    public List<MessageVO> getMessages(Long userId, Long otherId, int limit, int offset) {
        if (userId.equals(otherId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能查询与自己的对话");
        }
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        int safeOffset = Math.max(offset, 0);
        return privateMessageMapper.selectConversationMessages(userId, otherId, safeLimit, safeOffset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long recipientId, Long senderId) {
        privateMessageMapper.markAsRead(recipientId, senderId);
    }

    @Override
    public int countUnread(Long userId) {
        return privateMessageMapper.countUnread(userId);
    }

    @Override
    public List<ConversationVO> getConversations(Long userId) {
        return privateMessageMapper.selectConversations(userId);
    }

    // ──────────────────────────── WebSocket 推送 ──────────────────────────────

    /**
     * 通过 WebSocket 将新消息推送给收信方。
     *
     * @param recipientId 收信方用户 ID
     * @param vo          消息视图对象
     */
    private void pushMessageToRecipient(Long recipientId, MessageVO vo) {
        if (messagingTemplate == null) {
            return;
        }
        try {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(recipientId),
                    "/queue/messages",
                    vo
            );
        } catch (Exception e) {
            log.debug("Failed to push private message via WebSocket", e);
        }
    }
}
