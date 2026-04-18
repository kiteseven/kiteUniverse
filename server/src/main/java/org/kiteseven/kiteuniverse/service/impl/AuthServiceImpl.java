package org.kiteseven.kiteuniverse.service.impl;

import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.config.properties.AuthSecurityProperties;
import org.kiteseven.kiteuniverse.mapper.UserInfoMapper;
import org.kiteseven.kiteuniverse.mapper.UserMapper;
import org.kiteseven.kiteuniverse.mapper.UserSmsCodeMapper;
import org.kiteseven.kiteuniverse.pojo.dto.auth.PhoneLoginDTO;
import org.kiteseven.kiteuniverse.pojo.dto.auth.PhoneRegisterDTO;
import org.kiteseven.kiteuniverse.pojo.dto.auth.SmsCodeSendDTO;
import org.kiteseven.kiteuniverse.pojo.entity.User;
import org.kiteseven.kiteuniverse.pojo.entity.UserInfo;
import org.kiteseven.kiteuniverse.pojo.entity.UserSmsCode;
import org.kiteseven.kiteuniverse.pojo.vo.auth.AuthResultVO;
import org.kiteseven.kiteuniverse.pojo.vo.auth.AuthUserVO;
import org.kiteseven.kiteuniverse.pojo.vo.auth.SmsCodeSendVO;
import org.kiteseven.kiteuniverse.service.AuthService;
import org.kiteseven.kiteuniverse.support.auth.AuthCacheService;
import org.kiteseven.kiteuniverse.support.auth.UserTokenService;
import org.kiteseven.kiteuniverse.support.redis.CachePenetrationGuardService;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * Implements the phone verification code login and registration flow.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final int USER_STATUS_NORMAL = 1;
    private static final int SMS_STATUS_UNUSED = 0;
    private static final int SMS_STATUS_USED = 1;
    private static final int SMS_STATUS_EXPIRED = 2;
    private static final long SMS_EXPIRE_SECONDS = 300L;
    private static final String SCENE_LOGIN = "login";
    private static final String SCENE_REGISTER = "register";
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");
    private static final Pattern CODE_PATTERN = Pattern.compile("^\\d{6}$");
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final UserMapper userMapper;
    private final UserInfoMapper userInfoMapper;
    private final UserSmsCodeMapper userSmsCodeMapper;
    private final UserTokenService userTokenService;
    private final AuthCacheService authCacheService;
    private final AuthSecurityProperties authSecurityProperties;
    private final Environment environment;
    private final CachePenetrationGuardService cachePenetrationGuardService;

    public AuthServiceImpl(UserMapper userMapper,
                           UserInfoMapper userInfoMapper,
                           UserSmsCodeMapper userSmsCodeMapper,
                           UserTokenService userTokenService,
                           AuthCacheService authCacheService,
                           AuthSecurityProperties authSecurityProperties,
                           Environment environment,
                           CachePenetrationGuardService cachePenetrationGuardService) {
        this.userMapper = userMapper;
        this.userInfoMapper = userInfoMapper;
        this.userSmsCodeMapper = userSmsCodeMapper;
        this.userTokenService = userTokenService;
        this.authCacheService = authCacheService;
        this.authSecurityProperties = authSecurityProperties;
        this.environment = environment;
        this.cachePenetrationGuardService = cachePenetrationGuardService;
    }

    /**
     * Creates a verification code record for the target phone number.
     * The latest code is written to both MySQL and Redis, and Redis also enforces rate limits.
     *
     * @param smsCodeSendDTO request body
     * @param clientIp current client IP address
     * @return created code data
     */
    @Override
    public SmsCodeSendVO sendSmsCode(SmsCodeSendDTO smsCodeSendDTO, String clientIp) {
        String phone = normalizePhone(smsCodeSendDTO == null ? null : smsCodeSendDTO.getPhone());
        String scene = normalizeScene(smsCodeSendDTO == null ? null : smsCodeSendDTO.getScene());
        String normalizedClientIp = normalizeClientIp(clientIp);

        validatePhone(phone);
        validateSceneState(phone, scene);
        enforceSmsSendCooldown(phone, scene);
        enforceSmsDailyLimit(phone);
        enforceSmsIpWindowLimit(normalizedClientIp);

        String code = createRandomCode();
        UserSmsCode userSmsCode = new UserSmsCode();
        userSmsCode.setPhone(phone);
        userSmsCode.setBizType(scene);
        userSmsCode.setCode(code);
        userSmsCode.setStatus(SMS_STATUS_UNUSED);
        userSmsCode.setExpiresAt(LocalDateTime.now().plusSeconds(SMS_EXPIRE_SECONDS));
        userSmsCodeMapper.insert(userSmsCode);

        authCacheService.cacheSmsCode(phone, scene, code, SMS_EXPIRE_SECONDS);

        SmsCodeSendVO smsCodeSendVO = new SmsCodeSendVO();
        smsCodeSendVO.setPhone(phone);
        smsCodeSendVO.setScene(scene);
        smsCodeSendVO.setExpireSeconds(SMS_EXPIRE_SECONDS);
        smsCodeSendVO.setDebugMode(isDevelopmentEnvironment());
        smsCodeSendVO.setDebugCode(isDevelopmentEnvironment() ? code : null);
        return smsCodeSendVO;
    }

    /**
     * Registers a new user via phone verification and returns a token immediately.
     *
     * @param phoneRegisterDTO request body
     * @return authenticated session
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResultVO registerByPhone(PhoneRegisterDTO phoneRegisterDTO) {
        if (phoneRegisterDTO == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "注册参数不能为空");
        }

        String phone = normalizePhone(phoneRegisterDTO.getPhone());
        validatePhone(phone);

        if (userMapper.selectByPhone(phone) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该手机号已注册");
        }

        verifySmsCode(phone, SCENE_REGISTER, phoneRegisterDTO.getCode());

        User user = buildNewPhoneUser(phone, phoneRegisterDTO.getNickname());
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.insert(user);

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfoMapper.insert(userInfo);
        cachePenetrationGuardService.addUserId(user.getId());

        return buildAuthResult(user);
    }

    /**
     * Logs in an existing user after code verification.
     *
     * @param phoneLoginDTO request body
     * @return authenticated session
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResultVO loginByPhone(PhoneLoginDTO phoneLoginDTO) {
        if (phoneLoginDTO == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "登录参数不能为空");
        }

        String phone = normalizePhone(phoneLoginDTO.getPhone());
        validatePhone(phone);
        verifySmsCode(phone, SCENE_LOGIN, phoneLoginDTO.getCode());

        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "该手机号尚未注册");
        }
        ensureUserEnabled(user);

        LocalDateTime lastLoginTime = LocalDateTime.now();
        user.setLastLoginTime(lastLoginTime);
        userMapper.updateLastLoginTimeById(user.getId(), lastLoginTime);
        return buildAuthResult(user);
    }

    /**
     * Resolves the user from the current token.
     *
     * @param token raw client token
     * @return current user summary
     */
    @Override
    public AuthUserVO getCurrentUser(String token) {
        Long userId = userTokenService.parseUserId(token);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录用户不存在");
        }
        ensureUserEnabled(user);
        return buildAuthUser(user);
    }

    /**
     * Applies the Redis-backed SMS cooldown before a new code is generated.
     *
     * @param phone phone number
     * @param scene business scene
     */
    private void enforceSmsSendCooldown(String phone, String scene) {
        long cooldownSeconds = authSecurityProperties.getSmsSendCooldownSeconds();
        if (cooldownSeconds <= 0L) {
            return;
        }

        boolean acquired = authCacheService.tryAcquireSmsSendCooldown(phone, scene, cooldownSeconds);
        if (acquired) {
            return;
        }

        long remainingSeconds = Math.max(authCacheService.getSmsSendCooldownSeconds(phone, scene), 1L);
        throw new BusinessException(ResultCode.BAD_REQUEST, "请求过于频繁，请 " + remainingSeconds + " 秒后再试");
    }

    /**
     * Applies the per-phone daily SMS send limit.
     *
     * @param phone phone number
     */
    private void enforceSmsDailyLimit(String phone) {
        long dailyLimit = authSecurityProperties.getSmsDailyLimitPerPhone();
        if (dailyLimit <= 0L) {
            return;
        }

        long dailyCount = authCacheService.incrementSmsDailyCount(phone);
        if (dailyCount > dailyLimit) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该手机号今日验证码发送次数已达上限，请明天再试");
        }
    }

    /**
     * Applies the per-IP SMS rate limit within the configured Redis window.
     *
     * @param clientIp current client IP address
     */
    private void enforceSmsIpWindowLimit(String clientIp) {
        long ipWindowLimit = authSecurityProperties.getSmsIpWindowLimit();
        long ipWindowSeconds = authSecurityProperties.getSmsIpWindowSeconds();
        if (ipWindowLimit <= 0L || ipWindowSeconds <= 0L) {
            return;
        }

        long requestCount = authCacheService.incrementSmsIpWindowCount(clientIp, ipWindowSeconds);
        if (requestCount > ipWindowLimit) {
            long remainingSeconds = Math.max(authCacheService.getSmsIpWindowRemainingSeconds(clientIp), 1L);
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前网络发送过于频繁，请 " + remainingSeconds + " 秒后再试");
        }
    }

    /**
     * Validates the latest SMS code. Redis is used as the hot cache and MySQL remains the audit source.
     *
     * @param phone phone number
     * @param scene business scene
     * @param code submitted verification code
     */
    private void verifySmsCode(String phone, String scene, String code) {
        if (!StringUtils.hasText(code) || !CODE_PATTERN.matcher(code.trim()).matches()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请输入 6 位验证码");
        }

        UserSmsCode latestCode = userSmsCodeMapper.selectLatestUnused(phone, scene);
        if (latestCode == null) {
            authCacheService.clearSmsCode(phone, scene);
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先获取验证码");
        }

        if (latestCode.getExpiresAt() == null || latestCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            userSmsCodeMapper.updateStatus(latestCode.getId(), SMS_STATUS_EXPIRED, null);
            authCacheService.clearSmsCode(phone, scene);
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码已过期，请重新获取");
        }

        String cachedCode = authCacheService.getSmsCode(phone, scene);
        String expectedCode = StringUtils.hasText(cachedCode) ? cachedCode : latestCode.getCode();
        if (!expectedCode.equals(code.trim())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码不正确");
        }

        userSmsCodeMapper.updateStatus(latestCode.getId(), SMS_STATUS_USED, LocalDateTime.now());
        authCacheService.clearSmsCode(phone, scene);
    }

    /**
     * Ensures that login and registration scenes match the current account state.
     *
     * @param phone phone number
     * @param scene business scene
     */
    private void validateSceneState(String phone, String scene) {
        User user = userMapper.selectByPhone(phone);
        if (SCENE_REGISTER.equals(scene) && user != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该手机号已注册，请直接登录");
        }
        if (SCENE_LOGIN.equals(scene)) {
            if (user == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "该手机号尚未注册，请先完成注册");
            }
            ensureUserEnabled(user);
        }
    }

    /**
     * Builds a new account created by the phone-registration flow.
     *
     * @param phone phone number
     * @param nickname optional nickname
     * @return new user entity
     */
    private User buildNewPhoneUser(String phone, String nickname) {
        User user = new User();
        user.setUsername(createUniqueUsername(phone));
        user.setPassword(PASSWORD_ENCODER.encode(UUID.randomUUID().toString().replace("-", "")));
        user.setNickname(resolveNickname(phone, nickname));
        user.setPhone(phone);
        user.setGender(0);
        user.setStatus(USER_STATUS_NORMAL);
        return user;
    }

    /**
     * Builds the frontend session payload after successful authentication.
     *
     * @param user authenticated user
     * @return auth result
     */
    private AuthResultVO buildAuthResult(User user) {
        UserTokenService.AuthToken authToken = userTokenService.generateToken(user.getId());

        AuthResultVO authResultVO = new AuthResultVO();
        authResultVO.setToken(authToken.value());
        authResultVO.setTokenType("Bearer");
        authResultVO.setExpiresIn(authToken.expiresIn());
        authResultVO.setExpiresAt(authToken.expiresAt());
        authResultVO.setUser(buildAuthUser(user));
        return authResultVO;
    }

    /**
     * Maps the user entity to the lightweight auth summary returned to the frontend.
     *
     * @param user authenticated user
     * @return auth user summary
     */
    private AuthUserVO buildAuthUser(User user) {
        AuthUserVO authUserVO = new AuthUserVO();
        authUserVO.setId(user.getId());
        authUserVO.setUsername(user.getUsername());
        authUserVO.setNickname(user.getNickname());
        authUserVO.setPhone(user.getPhone());
        authUserVO.setAvatar(user.getAvatar());
        authUserVO.setStatus(user.getStatus());
        authUserVO.setRole(user.getRole() != null ? user.getRole() : "user");
        return authUserVO;
    }

    /**
     * Validates the phone number format.
     *
     * @param phone phone number
     */
    private void validatePhone(String phone) {
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请输入正确的 11 位手机号");
        }
    }

    /**
     * Ensures the user account is still enabled.
     *
     * @param user user entity
     */
    private void ensureUserEnabled(User user) {
        if (user.getStatus() == null || user.getStatus() != USER_STATUS_NORMAL) {
            throw new BusinessException(ResultCode.FORBIDDEN, "当前账号已被禁用");
        }
    }

    /**
     * Normalizes the phone number input.
     *
     * @param phone raw phone number
     * @return trimmed phone number
     */
    private String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "手机号不能为空");
        }
        return phone.trim();
    }

    /**
     * Normalizes the business scene and checks whether it is supported.
     *
     * @param scene raw business scene
     * @return normalized scene
     */
    private String normalizeScene(String scene) {
        if (!StringUtils.hasText(scene)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码场景不能为空");
        }
        String normalizedScene = scene.trim().toLowerCase();
        if (!SCENE_LOGIN.equals(normalizedScene) && !SCENE_REGISTER.equals(normalizedScene)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码场景不支持");
        }
        return normalizedScene;
    }

    /**
     * Normalizes the current client IP address.
     *
     * @param clientIp raw client IP address
     * @return normalized client IP
     */
    private String normalizeClientIp(String clientIp) {
        if (!StringUtils.hasText(clientIp)) {
            return "unknown";
        }
        return clientIp.trim();
    }

    /**
     * Resolves the display nickname for a phone-registered account.
     *
     * @param phone phone number
     * @param nickname optional nickname
     * @return final nickname
     */
    private String resolveNickname(String phone, String nickname) {
        if (StringUtils.hasText(nickname)) {
            return nickname.trim();
        }
        return "风筝用户" + phone.substring(phone.length() - 4);
    }

    /**
     * Generates a unique username based on the phone number.
     *
     * @param phone phone number
     * @return unique username
     */
    private String createUniqueUsername(String phone) {
        String candidate = "ku_" + phone;
        if (userMapper.selectByUsername(candidate) == null) {
            return candidate;
        }
        return candidate + "_" + System.currentTimeMillis();
    }

    /**
     * Creates a random six-digit verification code.
     *
     * @return verification code
     */
    private String createRandomCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }

    /**
     * Returns whether the backend is currently running with the development profile.
     *
     * @return true when the dev profile is active
     */
    private boolean isDevelopmentEnvironment() {
        return environment.acceptsProfiles(Profiles.of("dev"));
    }
}
