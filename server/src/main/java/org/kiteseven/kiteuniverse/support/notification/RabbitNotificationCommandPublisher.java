package org.kiteseven.kiteuniverse.support.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kiteseven.kiteuniverse.config.properties.NotificationMqProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ-backed notification publisher with direct-write fallback when MQ publish fails.
 */
@Service
@Conditional(NotificationMqAvailableCondition.class)
public class RabbitNotificationCommandPublisher
        extends AbstractTransactionAwareNotificationCommandPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitNotificationCommandPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationMqProperties notificationMqProperties;
    private final NotificationCommandProcessor notificationCommandProcessor;

    public RabbitNotificationCommandPublisher(RabbitTemplate rabbitTemplate,
                                              ObjectMapper objectMapper,
                                              NotificationMqProperties notificationMqProperties,
                                              NotificationCommandProcessor notificationCommandProcessor) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.notificationMqProperties = notificationMqProperties;
        this.notificationCommandProcessor = notificationCommandProcessor;
    }

    @Override
    protected void doPublish(NotificationCommand notificationCommand) {
        try {
            rabbitTemplate.convertAndSend(
                    notificationMqProperties.getExchange(),
                    notificationMqProperties.getRoutingKey(),
                    objectMapper.writeValueAsString(notificationCommand)
            );
        } catch (Exception exception) {
            log.warn("RabbitMQ publish failed for notification command {}, falling back to direct processing",
                    notificationCommand.getType(), exception);
            notificationCommandProcessor.process(notificationCommand);
        }
    }
}
