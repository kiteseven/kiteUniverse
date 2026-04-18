package org.kiteseven.kiteuniverse.support.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class NotificationCommandListenerTest {

    @Mock
    private NotificationCommandProcessor notificationCommandProcessor;

    private ObjectMapper objectMapper;
    private NotificationCommandListener notificationCommandListener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        notificationCommandListener = new NotificationCommandListener(objectMapper, notificationCommandProcessor);
    }

    @Test
    void shouldDeserializeAndDispatchNotificationCommand() throws Exception {
        NotificationCommand command = NotificationCommand.follow(1L, 2L);

        notificationCommandListener.onMessage(objectMapper.writeValueAsString(command));

        ArgumentCaptor<NotificationCommand> commandCaptor = ArgumentCaptor.forClass(NotificationCommand.class);
        verify(notificationCommandProcessor).process(commandCaptor.capture());
        NotificationCommand consumedCommand = commandCaptor.getValue();
        assertEquals(NotificationCommand.Type.FOLLOW, consumedCommand.getType());
        assertEquals(1L, consumedCommand.getSenderId());
        assertEquals(2L, consumedCommand.getRecipientId());
    }

    @Test
    void shouldRejectMalformedMessagesWithoutRequeue() {
        assertThrows(
                AmqpRejectAndDontRequeueException.class,
                () -> notificationCommandListener.onMessage("{not-json}")
        );
        verifyNoMoreInteractions(notificationCommandProcessor);
    }
}
