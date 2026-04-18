package org.kiteseven.kiteuniverse.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.mapper.UserFollowMapper;
import org.kiteseven.kiteuniverse.mapper.UserInfoMapper;
import org.kiteseven.kiteuniverse.mapper.UserMapper;
import org.kiteseven.kiteuniverse.pojo.dto.user.UserRegisterDTO;
import org.kiteseven.kiteuniverse.pojo.entity.User;
import org.kiteseven.kiteuniverse.support.redis.CachePenetrationGuardService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserInfoMapper userInfoMapper;

    @Mock
    private UserFollowMapper userFollowMapper;

    @Mock
    private CachePenetrationGuardService cachePenetrationGuardService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserDetailShouldShortCircuitWhenBloomFilterRejectsId() {
        when(cachePenetrationGuardService.mightContainUserId(77L)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.getUserDetail(77L));

        assertEquals(404, exception.getCode());
        verify(userMapper, never()).selectById(77L);
    }

    @Test
    void registerUserShouldAddCreatedUserIdToBloomFilter() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("kite_user");
        dto.setPassword("123456");
        dto.setNickname("Kite");
        dto.setEmail("kite@example.com");
        dto.setPhone("13800000000");

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(123L);
            return 1;
        }).when(userMapper).insert(any(User.class));

        Long userId = userService.registerUser(dto);

        assertEquals(123L, userId);
        verify(cachePenetrationGuardService).addUserId(123L);
    }
}
