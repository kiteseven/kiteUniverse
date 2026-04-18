package org.kiteseven.kiteuniverse.support.notification;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Base publisher that defers execution until after commit when a transaction is active.
 */
public abstract class AbstractTransactionAwareNotificationCommandPublisher
        implements NotificationCommandPublisher {

    @Override
    public void publishAfterCommit(NotificationCommand notificationCommand) {
        Runnable publishAction = () -> doPublish(notificationCommand);
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishAction.run();
                }
            });
            return;
        }
        publishAction.run();
    }

    protected abstract void doPublish(NotificationCommand notificationCommand);
}
