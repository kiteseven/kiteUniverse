package org.kiteseven.kiteuniverse.support.notification;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Enables direct notification processing when MQ is disabled or unreachable.
 */
public class NotificationMqUnavailableCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return !NotificationMqConditionSupport.isMqEnabled(context.getEnvironment())
                || !NotificationMqConditionSupport.isBrokerReachable(context.getEnvironment());
    }
}
