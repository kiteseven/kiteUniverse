package org.kiteseven.kiteuniverse.support.notification;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Enables RabbitMQ-backed notification delivery only when MQ is configured and reachable.
 */
public class NotificationMqAvailableCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return NotificationMqConditionSupport.isMqEnabled(context.getEnvironment())
                && NotificationMqConditionSupport.isBrokerReachable(context.getEnvironment());
    }
}
