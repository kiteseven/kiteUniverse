package org.kiteseven.kiteuniverse.config;

import org.kiteseven.kiteuniverse.config.properties.NotificationMqProperties;
import org.kiteseven.kiteuniverse.support.notification.NotificationMqAvailableCondition;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares RabbitMQ exchanges, queues, and DLQ bindings used by async notifications.
 */
@Configuration
@Conditional(NotificationMqAvailableCondition.class)
public class NotificationMqConfig {

    @Bean
    public DirectExchange notificationCommandExchange(NotificationMqProperties notificationMqProperties) {
        return new DirectExchange(notificationMqProperties.getExchange(), true, false);
    }

    @Bean
    public DirectExchange notificationCommandDeadLetterExchange(NotificationMqProperties notificationMqProperties) {
        return new DirectExchange(notificationMqProperties.getDeadLetterExchange(), true, false);
    }

    @Bean
    public Queue notificationCommandQueue(NotificationMqProperties notificationMqProperties) {
        return QueueBuilder.durable(notificationMqProperties.getQueue())
                .deadLetterExchange(notificationMqProperties.getDeadLetterExchange())
                .deadLetterRoutingKey(notificationMqProperties.getDeadLetterRoutingKey())
                .build();
    }

    @Bean
    public Queue notificationCommandDeadLetterQueue(NotificationMqProperties notificationMqProperties) {
        return QueueBuilder.durable(notificationMqProperties.getDeadLetterQueue()).build();
    }

    @Bean
    public Binding notificationCommandBinding(Queue notificationCommandQueue,
                                              DirectExchange notificationCommandExchange,
                                              NotificationMqProperties notificationMqProperties) {
        return BindingBuilder.bind(notificationCommandQueue)
                .to(notificationCommandExchange)
                .with(notificationMqProperties.getRoutingKey());
    }

    @Bean
    public Binding notificationCommandDeadLetterBinding(Queue notificationCommandDeadLetterQueue,
                                                        DirectExchange notificationCommandDeadLetterExchange,
                                                        NotificationMqProperties notificationMqProperties) {
        return BindingBuilder.bind(notificationCommandDeadLetterQueue)
                .to(notificationCommandDeadLetterExchange)
                .with(notificationMqProperties.getDeadLetterRoutingKey());
    }
}
