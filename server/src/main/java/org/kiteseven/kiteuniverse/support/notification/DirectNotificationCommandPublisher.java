package org.kiteseven.kiteuniverse.support.notification;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

/**
 * Local fallback publisher used when RabbitMQ delivery is disabled or unavailable.
 */
@Service
@Conditional(NotificationMqUnavailableCondition.class)
public class DirectNotificationCommandPublisher
        extends AbstractTransactionAwareNotificationCommandPublisher {

    private final NotificationCommandProcessor notificationCommandProcessor;

    public DirectNotificationCommandPublisher(NotificationCommandProcessor notificationCommandProcessor) {
        this.notificationCommandProcessor = notificationCommandProcessor;
    }

    @Override
    protected void doPublish(NotificationCommand notificationCommand) {
        notificationCommandProcessor.process(notificationCommand);
    }
}
