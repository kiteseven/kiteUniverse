package org.kiteseven.kiteuniverse.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiteseven.kiteuniverse.config.properties.AuthSecurityProperties;
import org.kiteseven.kiteuniverse.mapper.UserInfoMapper;
import org.kiteseven.kiteuniverse.mapper.UserMapper;
import org.kiteseven.kiteuniverse.mapper.UserSmsCodeMapper;
import org.kiteseven.kiteuniverse.pojo.dto.auth.PhoneRegisterDTO;
import org.kiteseven.kiteuniverse.pojo.entity.User;
import org.kiteseven.kiteuniverse.pojo.entity.UserSmsCode;
import org.kiteseven.kiteuniverse.pojo.vo.auth.AuthResultVO;
import org.kiteseven.kiteuniverse.support.auth.AuthCacheService;
import org.kiteseven.kiteuniverse.support.auth.UserTokenService;
import org.kiteseven.kiteuniverse.support.redis.CachePenetrationGuardService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserInfoMapper userInfoMapper;

    @Mock
    private UserSmsCodeMapper userSmsCodeMapper;

    @Mock
    private UserTokenService userTokenService;

    @Mock
    private AuthCacheService authCacheService;

    @Mock
    private AuthSecurityProperties authSecurityProperties;

    @Mock
    private Environment environment;

    @Mock
    private CachePenetrationGuardService cachePenetrationGuardService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerByPhoneShouldAddCreatedUserIdToBloomFilter() {
        PhoneRegisterDTO dto = new PhoneRegisterDTO();
        dto.setPhone("13800000000");
        dto.setCode("123456");
        dto.setNickname("Kite");

        UserSmsCode smsCode = new UserSmsCode();
        smsCode.setId(9L);
        smsCode.setCode("123456");
        smsCode.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(userMapper.selectByPhone("13800000000")).thenReturn(null);
        when(userSmsCodeMapper.selectLatestUnused("13800000000", "register")).thenReturn(smsCode);
        when(authCacheService.getSmsCode("13800000000", "register")).thenReturn("123456");
        when(userTokenService.generateToken(88L)).thenReturn(
                new UserTokenService.AuthToken("mock-token", 3600L, LocalDateTime.now().plusHours(1))
        );

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(88L);
            return 1;
        }).when(userMapper).insert(any(User.class));

        AuthResultVO result = authService.registerByPhone(dto);

        assertEquals("mock-token", result.getToken());
        verify(userSmsCodeMapper).updateStatus(eq(9L), eq(1), any(LocalDateTime.class));
        verify(cachePenetrationGuardService).addUserId(88L);
    }
}
