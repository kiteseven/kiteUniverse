package org.kiteseven.kiteuniverse.service.impl;

import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.mapper.UserFollowMapper;
import org.kiteseven.kiteuniverse.mapper.UserInfoMapper;
import org.kiteseven.kiteuniverse.mapper.UserMapper;
import org.kiteseven.kiteuniverse.pojo.dto.user.UserInfoUpdateDTO;
import org.kiteseven.kiteuniverse.pojo.dto.user.UserRegisterDTO;
import org.kiteseven.kiteuniverse.pojo.entity.User;
import org.kiteseven.kiteuniverse.pojo.entity.UserInfo;
import org.kiteseven.kiteuniverse.pojo.vo.user.UserDetailVO;
import org.kiteseven.kiteuniverse.service.UserService;
import org.kiteseven.kiteuniverse.support.redis.CachePenetrationGuardService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Default user service implementation for registration and profile maintenance.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final UserMapper userMapper;
    private final UserInfoMapper userInfoMapper;
    private final UserFollowMapper userFollowMapper;
    private final CachePenetrationGuardService cachePenetrationGuardService;

    public UserServiceImpl(UserMapper userMapper,
                           UserInfoMapper userInfoMapper,
                           UserFollowMapper userFollowMapper,
                           CachePenetrationGuardService cachePenetrationGuardService) {
        this.userMapper = userMapper;
        this.userInfoMapper = userInfoMapper;
        this.userFollowMapper = userFollowMapper;
        this.cachePenetrationGuardService = cachePenetrationGuardService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerUser(UserRegisterDTO userRegisterDTO) {
        validateRegisterParam(userRegisterDTO);
        checkDuplicateUser(userRegisterDTO);

        User user = new User();
        user.setUsername(userRegisterDTO.getUsername().trim());
        user.setPassword(PASSWORD_ENCODER.encode(userRegisterDTO.getPassword()));
        user.setNickname(StringUtils.hasText(userRegisterDTO.getNickname())
                ? userRegisterDTO.getNickname().trim()
                : userRegisterDTO.getUsername().trim());
        user.setEmail(normalizeText(userRegisterDTO.getEmail()));
        user.setPhone(normalizeText(userRegisterDTO.getPhone()));
        user.setGender(0);
        user.setStatus(1);
        userMapper.insert(user);

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfoMapper.insert(userInfo);
        cachePenetrationGuardService.addUserId(user.getId());
        return user.getId();
    }

    @Override
    public UserDetailVO getUserDetail(Long userId) {
        User user = getExistingUser(userId);
        UserInfo userInfo = userInfoMapper.selectByUserId(userId);

        UserDetailVO userDetailVO = new UserDetailVO();
        userDetailVO.setId(user.getId());
        userDetailVO.setUsername(user.getUsername());
        userDetailVO.setNickname(user.getNickname());
        userDetailVO.setEmail(user.getEmail());
        userDetailVO.setPhone(user.getPhone());
        userDetailVO.setAvatar(user.getAvatar());
        userDetailVO.setGender(user.getGender());
        userDetailVO.setStatus(user.getStatus());
        userDetailVO.setLastLoginTime(user.getLastLoginTime());
        userDetailVO.setCreateTime(user.getCreateTime());
        userDetailVO.setUpdateTime(user.getUpdateTime());

        if (userInfo != null) {
            userDetailVO.setRealName(userInfo.getRealName());
            userDetailVO.setBirthday(userInfo.getBirthday());
            userDetailVO.setSignature(userInfo.getSignature());
            userDetailVO.setProfile(userInfo.getProfile());
            userDetailVO.setCountry(userInfo.getCountry());
            userDetailVO.setProvince(userInfo.getProvince());
            userDetailVO.setCity(userInfo.getCity());
            userDetailVO.setWebsite(userInfo.getWebsite());
            userDetailVO.setBackgroundImage(userInfo.getBackgroundImage());
        }

        userDetailVO.setFollowerCount(userFollowMapper.countFollowers(userId));
        userDetailVO.setFollowingCount(userFollowMapper.countFollowing(userId));
        return userDetailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(Long userId, UserInfoUpdateDTO userInfoUpdateDTO) {
        if (userInfoUpdateDTO == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Update payload must not be null");
        }

        User user = getExistingUser(userId);
        mergeUserBaseInfo(user, userInfoUpdateDTO);
        userMapper.updateProfileById(user);

        UserInfo userInfo = userInfoMapper.selectByUserId(userId);
        if (userInfo == null) {
            userInfo = new UserInfo();
            userInfo.setUserId(userId);
            mergeUserInfo(userInfo, userInfoUpdateDTO);
            userInfoMapper.insert(userInfo);
            return;
        }

        mergeUserInfo(userInfo, userInfoUpdateDTO);
        userInfoMapper.updateByUserId(userInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserAvatar(Long userId, String avatar) {
        User user = getExistingUser(userId);
        user.setAvatar(normalizeText(avatar));
        userMapper.updateProfileById(user);
    }

    private void validateRegisterParam(UserRegisterDTO userRegisterDTO) {
        if (userRegisterDTO == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Register payload must not be null");
        }
        if (!StringUtils.hasText(userRegisterDTO.getUsername())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Username must not be blank");
        }
        if (!StringUtils.hasText(userRegisterDTO.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Password must not be blank");
        }
    }

    private void checkDuplicateUser(UserRegisterDTO userRegisterDTO) {
        if (userMapper.selectByUsername(userRegisterDTO.getUsername().trim()) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Username already exists");
        }
        if (StringUtils.hasText(userRegisterDTO.getEmail())
                && userMapper.selectByEmail(userRegisterDTO.getEmail().trim()) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Email already exists");
        }
        if (StringUtils.hasText(userRegisterDTO.getPhone())
                && userMapper.selectByPhone(userRegisterDTO.getPhone().trim()) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Phone number already exists");
        }
    }

    private User getExistingUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "User id is required");
        }
        if (userId > 0L && !cachePenetrationGuardService.mightContainUserId(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "User does not exist");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "User does not exist");
        }
        return user;
    }

    private void mergeUserBaseInfo(User user, UserInfoUpdateDTO userInfoUpdateDTO) {
        if (userInfoUpdateDTO.getNickname() != null) {
            user.setNickname(normalizeText(userInfoUpdateDTO.getNickname()));
        }
        if (userInfoUpdateDTO.getAvatar() != null) {
            user.setAvatar(normalizeText(userInfoUpdateDTO.getAvatar()));
        }
        if (userInfoUpdateDTO.getGender() != null) {
            user.setGender(userInfoUpdateDTO.getGender());
        }
    }

    private void mergeUserInfo(UserInfo userInfo, UserInfoUpdateDTO userInfoUpdateDTO) {
        if (userInfoUpdateDTO.getRealName() != null) {
            userInfo.setRealName(normalizeText(userInfoUpdateDTO.getRealName()));
        }
        if (userInfoUpdateDTO.getBirthday() != null) {
            userInfo.setBirthday(userInfoUpdateDTO.getBirthday());
        }
        if (userInfoUpdateDTO.getSignature() != null) {
            userInfo.setSignature(normalizeText(userInfoUpdateDTO.getSignature()));
        }
        if (userInfoUpdateDTO.getProfile() != null) {
            userInfo.setProfile(normalizeText(userInfoUpdateDTO.getProfile()));
        }
        if (userInfoUpdateDTO.getCountry() != null) {
            userInfo.setCountry(normalizeText(userInfoUpdateDTO.getCountry()));
        }
        if (userInfoUpdateDTO.getProvince() != null) {
            userInfo.setProvince(normalizeText(userInfoUpdateDTO.getProvince()));
        }
        if (userInfoUpdateDTO.getCity() != null) {
            userInfo.setCity(normalizeText(userInfoUpdateDTO.getCity()));
        }
        if (userInfoUpdateDTO.getWebsite() != null) {
            userInfo.setWebsite(normalizeText(userInfoUpdateDTO.getWebsite()));
        }
        if (userInfoUpdateDTO.getBackgroundImage() != null) {
            userInfo.setBackgroundImage(normalizeText(userInfoUpdateDTO.getBackgroundImage()));
        }
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
