package org.kiteseven.kiteuniverse.support.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shared RabbitMQ availability checks used to choose async or direct notification delivery.
 */
final class NotificationMqConditionSupport {

    private static final Logger log = LoggerFactory.getLogger(NotificationMqConditionSupport.class);

    private static final AtomicBoolean UNAVAILABLE_LOGGED = new AtomicBoolean(false);

    private NotificationMqConditionSupport() {
    }

    static boolean isMqEnabled(Environment environment) {
        return environment.getProperty("kite-universe.notification.mq.enabled", Boolean.class, true);
    }

    static boolean isBrokerReachable(Environment environment) {
        String host = environment.getProperty(
                "spring.rabbitmq.host",
                environment.getProperty("kite-universe.rabbitmq.host", "localhost")
        );
        int port = environment.getProperty(
                "spring.rabbitmq.port",
                Integer.class,
                environment.getProperty("kite-universe.rabbitmq.port", Integer.class, 5672)
        );
        int timeoutMillis = environment.getProperty(
                "kite-universe.notification.mq.startup-connect-timeout-millis",
                Integer.class,
                300
        );

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMillis);
            return true;
        } catch (Exception exception) {
            if (UNAVAILABLE_LOGGED.compareAndSet(false, true)) {
                log.warn(
                        "RabbitMQ notification delivery is enabled but broker {}:{} is unreachable at startup, "
                                + "falling back to direct post-commit notification processing",
                        host,
                        port
                );
            }
            return false;
        }
    }
}
