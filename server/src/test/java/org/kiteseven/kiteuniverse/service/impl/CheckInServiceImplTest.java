package org.kiteseven.kiteuniverse.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiteseven.kiteuniverse.mapper.DailyCheckInMapper;
import org.kiteseven.kiteuniverse.mapper.UserBadgeMapper;
import org.kiteseven.kiteuniverse.mapper.UserPointsMapper;
import org.kiteseven.kiteuniverse.pojo.entity.UserBadge;
import org.kiteseven.kiteuniverse.pojo.entity.UserPoints;
import org.kiteseven.kiteuniverse.pojo.vo.checkin.CheckInResultVO;
import org.kiteseven.kiteuniverse.support.redis.DistributedLockService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckInServiceImplTest {

    @Mock
    private UserPointsMapper userPointsMapper;

    @Mock
    private DailyCheckInMapper dailyCheckInMapper;

    @Mock
    private UserBadgeMapper userBadgeMapper;

    @Mock
    private DistributedLockService distributedLockService;

    @InjectMocks
    private CheckInServiceImpl checkInService;

    @Test
    void checkInShouldUseUserScopedLock() {
        UserPoints userPoints = new UserPoints();
        userPoints.setUserId(5L);
        userPoints.setPoints(0);
        userPoints.setLevel(1);
        userPoints.setTotalPointsEarned(0);
        userPoints.setConsecutiveDays(0);

        UserBadge badge = new UserBadge();
        badge.setUserId(5L);
        badge.setBadgeType("FIRST_CHECKIN");

        when(userPointsMapper.selectByUserId(5L)).thenReturn(userPoints);
        when(userBadgeMapper.selectByUserIdAndType(5L, "FIRST_CHECKIN")).thenReturn(null, badge);
        when(distributedLockService.executeWithLock(
                eq("user-progress:user:5"),
                anySupplier()
        )).thenAnswer(invocation -> ((Supplier<CheckInResultVO>) invocation.getArgument(1)).get());

        CheckInResultVO result = checkInService.checkIn(5L);

        assertEquals(10, result.getPointsEarned());
        assertEquals(1, result.getConsecutiveDays());
        verify(userPointsMapper).update(userPoints);
        verify(dailyCheckInMapper).insert(any());
        verify(userBadgeMapper).insert(any(UserBadge.class));
    }

    @Test
    void addPointsShouldUseSameUserScopedLock() {
        UserPoints userPoints = new UserPoints();
        userPoints.setUserId(9L);
        userPoints.setPoints(95);
        userPoints.setLevel(1);
        userPoints.setTotalPointsEarned(95);
        userPoints.setConsecutiveDays(0);

        UserBadge badge = new UserBadge();
        badge.setUserId(9L);
        badge.setBadgeType("LEVEL_2");

        when(userPointsMapper.selectByUserId(9L)).thenReturn(userPoints);
        when(userBadgeMapper.selectByUserIdAndType(9L, "LEVEL_2")).thenReturn(null, badge);
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(distributedLockService).runWithLock(eq("user-progress:user:9"), any(Runnable.class));

        checkInService.addPoints(9L, 10);

        assertEquals(105, userPoints.getPoints());
        assertEquals(2, userPoints.getLevel());
        assertEquals(105, userPoints.getTotalPointsEarned());
        verify(userPointsMapper).update(userPoints);
        verify(userBadgeMapper).insert(any(UserBadge.class));
    }

    @SuppressWarnings("unchecked")
    private Supplier<CheckInResultVO> anySupplier() {
        return any(Supplier.class);
    }
}
