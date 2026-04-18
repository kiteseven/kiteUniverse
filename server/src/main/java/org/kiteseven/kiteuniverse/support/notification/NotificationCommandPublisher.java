package org.kiteseven.kiteuniverse.support.notification;

/**
 * Publishes notification commands after the surrounding business transaction commits.
 */
public interface NotificationCommandPublisher {

    /**
     * Publishes the command immediately or after commit when a transaction is active.
     *
     * @param notificationCommand async notification command
     */
    void publishAfterCommit(NotificationCommand notificationCommand);
}
