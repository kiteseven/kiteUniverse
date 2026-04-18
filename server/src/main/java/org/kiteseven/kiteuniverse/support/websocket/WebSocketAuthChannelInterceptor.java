package org.kiteseven.kiteuniverse.support.websocket;

import org.kiteseven.kiteuniverse.support.auth.UserTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 在 STOMP CONNECT 帧到达时解析 Authorization 头中的 JWT，
 * 并将解析出的用户 ID 设置为 WebSocket 会话的 Principal。
 */
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserTokenService userTokenService;

    public WebSocketAuthChannelInterceptor(UserTokenService userTokenService) {
        this.userTokenService = userTokenService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return message;
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        try {
            Long userId = userTokenService.parseUserId(token);
            accessor.setUser(new StompPrincipal(userId));
        } catch (Exception e) {
            log.debug("WebSocket CONNECT auth failed: {}", e.getMessage());
        }
        return message;
    }
}
