package org.kiteseven.kiteuniverse.service.impl;

import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.mapper.UserInfoMapper;
import org.kiteseven.kiteuniverse.mapper.UserMapper;
import org.kiteseven.kiteuniverse.pojo.dto.user.UserInfoUpdateDTO;
import org.kiteseven.kiteuniverse.pojo.dto.user.UserRegisterDTO;
import org.kiteseven.kiteuniverse.pojo.entity.User;
import org.kiteseven.kiteuniverse.pojo.entity.UserInfo;
import org.kiteseven.kiteuniverse.pojo.vo.user.UserDetailVO;
import org.kiteseven.kiteuniverse.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Default user service implementation for account registration and personal-center updates.
 */
@Service
public class UserServiceImpl implements UserService {

    /**
     * Password encoder reused by the legacy username registration flow.
     */
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final UserMapper userMapper;
    private final UserInfoMapper userInfoMapper;

    public UserServiceImpl(UserMapper userMapper, UserInfoMapper userInfoMapper) {
        this.userMapper = userMapper;
        this.userInfoMapper = userInfoMapper;
    }

    /**
     * Registers a user account and creates an empty profile row.
     *
     * @param userRegisterDTO request body
     * @return created user id
     */
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
        return user.getId();
    }

    /**
     * Loads the combined account and profile detail data.
     *
     * @param userId user id
     * @return user detail
     */
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
        return userDetailVO;
    }

    /**
     * Updates editable display fields on the account and profile tables.
     *
     * @param userId user id
     * @param userInfoUpdateDTO request body
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(Long userId, UserInfoUpdateDTO userInfoUpdateDTO) {
        if (userInfoUpdateDTO == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "更新参数不能为空");
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

    /**
     * Updates the avatar path on the account table.
     *
     * @param userId user id
     * @param avatar avatar public path
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserAvatar(Long userId, String avatar) {
        User user = getExistingUser(userId);
        user.setAvatar(normalizeText(avatar));
        userMapper.updateProfileById(user);
    }

    /**
     * Validates the legacy registration request.
     *
     * @param userRegisterDTO request body
     */
    private void validateRegisterParam(UserRegisterDTO userRegisterDTO) {
        if (userRegisterDTO == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "注册参数不能为空");
        }
        if (!StringUtils.hasText(userRegisterDTO.getUsername())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名不能为空");
        }
        if (!StringUtils.hasText(userRegisterDTO.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "密码不能为空");
        }
    }

    /**
     * Ensures username, email, and phone stay unique.
     *
     * @param userRegisterDTO request body
     */
    private void checkDuplicateUser(UserRegisterDTO userRegisterDTO) {
        if (userMapper.selectByUsername(userRegisterDTO.getUsername().trim()) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }
        if (StringUtils.hasText(userRegisterDTO.getEmail())
                && userMapper.selectByEmail(userRegisterDTO.getEmail().trim()) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "邮箱已存在");
        }
        if (StringUtils.hasText(userRegisterDTO.getPhone())
                && userMapper.selectByPhone(userRegisterDTO.getPhone().trim()) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "手机号已存在");
        }
    }

    /**
     * Loads the existing account row or throws a user-not-found error.
     *
     * @param userId user id
     * @return user entity
     */
    private User getExistingUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户编号不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    /**
     * Merges editable account fields and only changes values explicitly submitted by the client.
     *
     * @param user account entity
     * @param userInfoUpdateDTO request body
     */
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

    /**
     * Merges editable profile fields and only changes values explicitly submitted by the client.
     *
     * @param userInfo profile entity
     * @param userInfoUpdateDTO request body
     */
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

    /**
     * Normalizes optional text fields and converts blank content to null.
     *
     * @param value raw text
     * @return normalized value
     */
    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
