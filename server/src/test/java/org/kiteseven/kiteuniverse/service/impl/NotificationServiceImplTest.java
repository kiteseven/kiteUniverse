package org.kiteseven.kiteuniverse.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiteseven.kiteuniverse.mapper.NotificationMapper;
import org.kiteseven.kiteuniverse.support.notification.NotificationCommand;
import org.kiteseven.kiteuniverse.support.notification.NotificationCommandPublisher;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationCommandPublisher notificationCommandPublisher;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void shouldPublishAsyncPostLikeCommand() {
        notificationService.createPostLikeNotification(1L, 3L, 2L);

        ArgumentCaptor<NotificationCommand> commandCaptor = ArgumentCaptor.forClass(NotificationCommand.class);
        verify(notificationCommandPublisher).publishAfterCommit(commandCaptor.capture());

        NotificationCommand command = commandCaptor.getValue();
        assertEquals(NotificationCommand.Type.POST_LIKE, command.getType());
        assertEquals(1L, command.getSenderId());
        assertEquals(2L, command.getRecipientId());
        assertEquals(3L, command.getPostId());
    }

    @Test
    void shouldSkipSelfNotification() {
        notificationService.createFollowNotification(7L, 7L);

        verify(notificationCommandPublisher, never()).publishAfterCommit(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldTrimAnnouncementBeforePublishing() {
        notificationService.publishAnnouncement("  hello world  ");

        ArgumentCaptor<NotificationCommand> commandCaptor = ArgumentCaptor.forClass(NotificationCommand.class);
        verify(notificationCommandPublisher).publishAfterCommit(commandCaptor.capture());
        assertEquals(NotificationCommand.Type.ANNOUNCEMENT, commandCaptor.getValue().getType());
        assertEquals("hello world", commandCaptor.getValue().getContent());
    }
}
