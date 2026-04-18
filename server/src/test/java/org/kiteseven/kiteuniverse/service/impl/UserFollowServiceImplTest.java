package org.kiteseven.kiteuniverse.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiteseven.kiteuniverse.mapper.UserFollowMapper;
import org.kiteseven.kiteuniverse.mapper.UserMapper;
import org.kiteseven.kiteuniverse.pojo.entity.User;
import org.kiteseven.kiteuniverse.pojo.vo.user.UserFollowStateVO;
import org.kiteseven.kiteuniverse.service.NotificationService;
import org.kiteseven.kiteuniverse.support.redis.DistributedLockService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFollowServiceImplTest {

    @Mock
    private UserFollowMapper userFollowMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private DistributedLockService distributedLockService;

    @InjectMocks
    private UserFollowServiceImpl userFollowService;

    @Test
    void followUserShouldUsePairScopedLock() {
        User currentUser = new User();
        currentUser.setId(1L);

        User targetUser = new User();
        targetUser.setId(2L);

        when(userMapper.selectById(1L)).thenReturn(currentUser);
        when(userMapper.selectById(2L)).thenReturn(targetUser);
        when(userFollowMapper.selectStatus(1L, 2L)).thenReturn(null);
        when(userFollowMapper.countFollowers(2L)).thenReturn(1);
        when(userFollowMapper.countFollowing(2L)).thenReturn(3);
        when(distributedLockService.executeWithLock(
                eq("user-follow:follower:1:following:2"),
                anySupplier()
        )).thenAnswer(invocation -> ((Supplier<UserFollowStateVO>) invocation.getArgument(1)).get());

        UserFollowStateVO result = userFollowService.followUser(1L, 2L);

        assertEquals(2L, result.getUserId());
        assertEquals(1, result.getFollowerCount());
        verify(userFollowMapper).insert(any());
        verify(notificationService).createFollowNotification(1L, 2L);
    }

    @SuppressWarnings("unchecked")
    private Supplier<UserFollowStateVO> anySupplier() {
        return any(Supplier.class);
    }
}
