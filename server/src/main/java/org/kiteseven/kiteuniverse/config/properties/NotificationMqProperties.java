package org.kiteseven.kiteuniverse.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings for async notification delivery over RabbitMQ.
 */
@ConfigurationProperties(prefix = "kite-universe.notification.mq")
public class NotificationMqProperties {

    private boolean enabled = true;

    private String exchange = "notification.command.exchange";

    private String queue = "notification.command.queue";

    private String routingKey = "notification.command";

    private String deadLetterExchange = "notification.command.dlx";

    private String deadLetterQueue = "notification.command.dlq";

    private String deadLetterRoutingKey = "notification.command.dlq";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getDeadLetterExchange() {
        return deadLetterExchange;
    }

    public void setDeadLetterExchange(String deadLetterExchange) {
        this.deadLetterExchange = deadLetterExchange;
    }

    public String getDeadLetterQueue() {
        return deadLetterQueue;
    }

    public void setDeadLetterQueue(String deadLetterQueue) {
        this.deadLetterQueue = deadLetterQueue;
    }

    public String getDeadLetterRoutingKey() {
        return deadLetterRoutingKey;
    }

    public void setDeadLetterRoutingKey(String deadLetterRoutingKey) {
        this.deadLetterRoutingKey = deadLetterRoutingKey;
    }
}
