package org.kiteseven.kiteuniverse.support.websocket;

import java.security.Principal;

/**
 * 代表通过 STOMP CONNECT 认证的用户身份，name 为用户 ID 字符串。
 */
public class StompPrincipal implements Principal {

    private final String name;

    public StompPrincipal(Long userId) {
        this.name = String.valueOf(userId);
    }

    @Override
    public String getName() {
        return name;
    }
}
