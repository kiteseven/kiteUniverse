package org.kiteseven.kiteuniverse.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.result.Result;
import org.kiteseven.kiteuniverse.pojo.dto.message.SendMessageDTO;
import org.kiteseven.kiteuniverse.pojo.vo.message.ConversationVO;
import org.kiteseven.kiteuniverse.pojo.vo.message.MessageVO;
import org.kiteseven.kiteuniverse.service.MessageService;
import org.kiteseven.kiteuniverse.support.auth.UserTokenService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 私信接口。
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserTokenService userTokenService;

    public MessageController(MessageService messageService, UserTokenService userTokenService) {
        this.messageService = messageService;
        this.userTokenService = userTokenService;
    }

    /** 发送私信。Body: {recipientId, content} */
    @PostMapping
    public Result<MessageVO> send(@RequestBody SendMessageDTO dto, HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        Long userId = userTokenService.parseUserId(token);
        return Result.success(messageService.sendMessage(userId, dto));
    }

    /** 获取与指定用户的聊天记录。 */
    @GetMapping("/{otherId}")
    public Result<List<MessageVO>> getMessages(
            @PathVariable Long otherId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset,
            HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        Long userId = userTokenService.parseUserId(token);
        return Result.success(messageService.getMessages(userId, otherId, limit, offset));
    }

    /** 将对方的消息全部标为已读。 */
    @PutMapping("/{senderId}/read")
    public Result<Void> markAsRead(@PathVariable Long senderId, HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        Long userId = userTokenService.parseUserId(token);
        messageService.markAsRead(userId, senderId);
        return Result.success(null);
    }

    /** 获取未读私信总数。 */
    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> unreadCount(HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        Long userId = userTokenService.parseUserId(token);
        return Result.success(Map.of("count", messageService.countUnread(userId)));
    }

    /** 获取会话列表。 */
    @GetMapping("/conversations")
    public Result<List<ConversationVO>> getConversations(HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        Long userId = userTokenService.parseUserId(token);
        return Result.success(messageService.getConversations(userId));
    }
}
