package org.kiteseven.kiteuniverse.service.impl;

import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.mapper.UserFollowMapper;
import org.kiteseven.kiteuniverse.mapper.UserMapper;
import org.kiteseven.kiteuniverse.pojo.entity.User;
import org.kiteseven.kiteuniverse.pojo.entity.UserFollow;
import org.kiteseven.kiteuniverse.pojo.vo.user.UserFollowItemVO;
import org.kiteseven.kiteuniverse.pojo.vo.user.UserFollowStateVO;
import org.kiteseven.kiteuniverse.service.NotificationService;
import org.kiteseven.kiteuniverse.service.UserFollowService;
import org.kiteseven.kiteuniverse.support.redis.DistributedLockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户关注服务实现。
 */
@Service
public class UserFollowServiceImpl implements UserFollowService {

    private static final int STATUS_ENABLED = 1;
    private static final int STATUS_DISABLED = 0;

    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final DistributedLockService distributedLockService;

    public UserFollowServiceImpl(UserFollowMapper userFollowMapper, UserMapper userMapper,
                                 NotificationService notificationService,
                                 DistributedLockService distributedLockService) {
        this.userFollowMapper = userFollowMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
        this.distributedLockService = distributedLockService;
    }

    @Override
    public UserFollowStateVO getFollowState(Long currentUserId, Long targetUserId) {
        getExistingUser(currentUserId);
        getExistingUser(targetUserId);
        Integer followStatus = userFollowMapper.selectStatus(currentUserId, targetUserId);
        return buildFollowState(targetUserId, followStatus != null && followStatus == STATUS_ENABLED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserFollowStateVO followUser(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能关注自己");
        }
        return distributedLockService.executeWithLock(buildFollowLockKey(currentUserId, targetUserId), () -> {
            getExistingUser(currentUserId);
            getExistingUser(targetUserId);
            Integer followStatus = userFollowMapper.selectStatus(currentUserId, targetUserId);
            boolean newFollow = false;
            if (followStatus == null) {
                UserFollow userFollow = new UserFollow();
                userFollow.setFollowerId(currentUserId);
                userFollow.setFollowingId(targetUserId);
                userFollow.setStatus(STATUS_ENABLED);
                userFollowMapper.insert(userFollow);
                newFollow = true;
            } else if (followStatus == STATUS_DISABLED) {
                userFollowMapper.activate(currentUserId, targetUserId);
                newFollow = true;
            }
            if (newFollow) {
                notificationService.createFollowNotification(currentUserId, targetUserId);
            }
            return buildFollowState(targetUserId, true);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserFollowStateVO unfollowUser(Long currentUserId, Long targetUserId) {
        return distributedLockService.executeWithLock(buildFollowLockKey(currentUserId, targetUserId), () -> {
            getExistingUser(currentUserId);
            getExistingUser(targetUserId);
            Integer followStatus = userFollowMapper.selectStatus(currentUserId, targetUserId);
            if (followStatus != null && followStatus == STATUS_ENABLED) {
                userFollowMapper.deactivate(currentUserId, targetUserId);
            }
            return buildFollowState(targetUserId, false);
        });
    }

    @Override
    public List<UserFollowItemVO> listFollowers(Long userId, int limit) {
        return userFollowMapper.selectFollowers(userId, Math.max(1, Math.min(limit, 50)));
    }

    @Override
    public List<UserFollowItemVO> listFollowing(Long userId, int limit) {
        return userFollowMapper.selectFollowing(userId, Math.max(1, Math.min(limit, 50)));
    }

    @Override
    public int countFollowers(Long userId) {
        return userFollowMapper.countFollowers(userId);
    }

    @Override
    public int countFollowing(Long userId) {
        return userFollowMapper.countFollowing(userId);
    }

    private User getExistingUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "目标用户不存在");
        }
        return user;
    }

    private UserFollowStateVO buildFollowState(Long targetUserId, boolean followed) {
        UserFollowStateVO vo = new UserFollowStateVO();
        vo.setUserId(targetUserId);
        vo.setFollowed(followed);
        vo.setFollowerCount(userFollowMapper.countFollowers(targetUserId));
        vo.setFollowingCount(userFollowMapper.countFollowing(targetUserId));
        return vo;
    }

    private String buildFollowLockKey(Long currentUserId, Long targetUserId) {
        return "user-follow:follower:" + currentUserId + ":following:" + targetUserId;
    }
}
