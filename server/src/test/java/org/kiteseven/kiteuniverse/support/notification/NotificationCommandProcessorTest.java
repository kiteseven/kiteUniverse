package org.kiteseven.kiteuniverse.support.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiteseven.kiteuniverse.mapper.NotificationMapper;
import org.kiteseven.kiteuniverse.mapper.UserMapper;
import org.kiteseven.kiteuniverse.pojo.entity.Notification;
import org.kiteseven.kiteuniverse.pojo.entity.User;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationCommandProcessorTest {

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    private NotificationCommandProcessor notificationCommandProcessor;

    @BeforeEach
    void setUp() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("simpMessagingTemplate", simpMessagingTemplate);
        notificationCommandProcessor = new NotificationCommandProcessor(
                notificationMapper,
                userMapper,
                beanFactory.getBeanProvider(SimpMessagingTemplate.class)
        );
    }

    @Test
    void shouldInsertLikeNotificationAndPushUnreadCount() {
        User sender = new User();
        sender.setId(1L);
        sender.setNickname("Alice");

        NotificationCommand command = NotificationCommand.postLike(1L, 2L, 3L);

        when(userMapper.selectById(1L)).thenReturn(sender);
        when(notificationMapper.countUnread(2L)).thenReturn(5);

        notificationCommandProcessor.process(command);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationMapper).insert(notificationCaptor.capture());
        Notification notification = notificationCaptor.getValue();
        assertEquals(2L, notification.getRecipientId());
        assertEquals(1L, notification.getSenderId());
        assertEquals("POST_LIKE", notification.getType());
        assertEquals(3L, notification.getPostId());
        assertEquals("Alice 点赞了你的帖子", notification.getContent());
        assertEquals(0, notification.getIsRead());
        verify(simpMessagingTemplate).convertAndSendToUser(
                "2",
                "/queue/notifications",
                Map.of("unreadCount", 5)
        );
    }

    @Test
    void shouldInsertAnnouncementForEveryUserAndBroadcastSystemMessage() {
        User firstUser = new User();
        firstUser.setId(10L);
        User secondUser = new User();
        secondUser.setId(20L);

        when(userMapper.selectAll()).thenReturn(List.of(firstUser, secondUser));

        notificationCommandProcessor.process(NotificationCommand.announcement("system maintenance"));

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationMapper, times(2)).insert(notificationCaptor.capture());
        List<Notification> notifications = notificationCaptor.getAllValues();
        assertEquals(List.of(10L, 20L), notifications.stream().map(Notification::getRecipientId).toList());
        assertTrue(notifications.stream().allMatch(notification -> "ANNOUNCEMENT".equals(notification.getType())));
        assertTrue(notifications.stream().allMatch(notification -> "system maintenance".equals(notification.getContent())));
        verify(simpMessagingTemplate).convertAndSend(
                "/topic/system",
                (Object) Map.of("type", "announcement", "content", "system maintenance")
        );
    }
}
