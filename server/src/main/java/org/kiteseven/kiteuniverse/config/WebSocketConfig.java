package org.kiteseven.kiteuniverse.config;

import org.kiteseven.kiteuniverse.support.websocket.WebSocketAuthChannelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * 配置基于 STOMP 协议的 WebSocket 消息代理。
 *
 * <p>客户端连接路径：{@code ws://<host>/ws}（原生 WebSocket，无 SockJS）
 * <p>用户消息目的地前缀：{@code /user}，例如 {@code /user/queue/notifications}
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthChannelInterceptor authChannelInterceptor;

    public WebSocketConfig(WebSocketAuthChannelInterceptor authChannelInterceptor) {
        this.authChannelInterceptor = authChannelInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 简单内存代理，支持 /topic（广播）和 /queue（单播）
        config.enableSimpleBroker("/topic", "/queue");
        // 客户端向服务端发消息时用 /app 前缀（本项目目前不需要客户端→服务端 STOMP）
        config.setApplicationDestinationPrefixes("/app");
        // 用户专属消息前缀
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);
    }
}
