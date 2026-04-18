package org.kiteseven.kiteuniverse.support.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiteseven.kiteuniverse.config.properties.NotificationMqProperties;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitNotificationCommandPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private NotificationCommandProcessor notificationCommandProcessor;

    private ObjectMapper objectMapper;
    private NotificationMqProperties notificationMqProperties;
    private RabbitNotificationCommandPublisher rabbitNotificationCommandPublisher;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        notificationMqProperties = new NotificationMqProperties();
        rabbitNotificationCommandPublisher = new RabbitNotificationCommandPublisher(
                rabbitTemplate,
                objectMapper,
                notificationMqProperties,
                notificationCommandProcessor
        );
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clear();
    }

    @Test
    void shouldPublishImmediatelyWhenNoTransactionIsActive() throws Exception {
        NotificationCommand command = NotificationCommand.postLike(1L, 2L, 3L);

        rabbitNotificationCommandPublisher.publishAfterCommit(command);

        verify(rabbitTemplate).convertAndSend(
                notificationMqProperties.getExchange(),
                notificationMqProperties.getRoutingKey(),
                objectMapper.writeValueAsString(command)
        );
        verify(notificationCommandProcessor, never()).process(command);
    }

    @Test
    void shouldDelayPublishUntilAfterCommitWhenTransactionIsActive() throws Exception {
        NotificationCommand command = NotificationCommand.follow(1L, 2L);
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        rabbitNotificationCommandPublisher.publishAfterCommit(command);

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }

        verify(rabbitTemplate).convertAndSend(
                notificationMqProperties.getExchange(),
                notificationMqProperties.getRoutingKey(),
                objectMapper.writeValueAsString(command)
        );
        verify(notificationCommandProcessor, never()).process(command);
    }

    @Test
    void shouldFallBackToDirectProcessingWhenRabbitPublishFails() {
        NotificationCommand command = NotificationCommand.comment(1L, 2L, 3L, 4L, "hello");
        doThrow(new IllegalStateException("mq down")).when(rabbitTemplate).convertAndSend(
                eq(notificationMqProperties.getExchange()),
                eq(notificationMqProperties.getRoutingKey()),
                anyString()
        );

        rabbitNotificationCommandPublisher.publishAfterCommit(command);

        verify(notificationCommandProcessor).process(command);
    }
}
