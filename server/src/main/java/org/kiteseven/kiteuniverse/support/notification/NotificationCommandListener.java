package org.kiteseven.kiteuniverse.support.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ consumer for async notification commands.
 */
@Component
@Conditional(NotificationMqAvailableCondition.class)
public class NotificationCommandListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationCommandListener.class);

    private final ObjectMapper objectMapper;
    private final NotificationCommandProcessor notificationCommandProcessor;

    public NotificationCommandListener(ObjectMapper objectMapper,
                                       NotificationCommandProcessor notificationCommandProcessor) {
        this.objectMapper = objectMapper;
        this.notificationCommandProcessor = notificationCommandProcessor;
    }

    @RabbitListener(queues = "#{notificationCommandQueue.name}")
    public void onMessage(String rawMessage) {
        try {
            NotificationCommand notificationCommand =
                    objectMapper.readValue(rawMessage, NotificationCommand.class);
            notificationCommandProcessor.process(notificationCommand);
        } catch (Exception exception) {
            log.warn("Failed to consume notification command", exception);
            throw new AmqpRejectAndDontRequeueException("Failed to consume notification command", exception);
        }
    }
}
