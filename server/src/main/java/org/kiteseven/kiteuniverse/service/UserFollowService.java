package org.kiteseven.kiteuniverse.service;

import org.kiteseven.kiteuniverse.pojo.vo.user.UserFollowItemVO;
import org.kiteseven.kiteuniverse.pojo.vo.user.UserFollowStateVO;

import java.util.List;

/**
 * 提供用户关注与取消关注能力。
 */
public interface UserFollowService {

    /**
     * 查询当前用户对目标用户的关注状态。
     *
     * @param currentUserId 当前用户编号
     * @param targetUserId 目标用户编号
     * @return 关注状态
     */
    UserFollowStateVO getFollowState(Long currentUserId, Long targetUserId);

    /**
     * 关注用户。
     *
     * @param currentUserId 当前用户编号
     * @param targetUserId 目标用户编号
     * @return 关注状态
     */
    UserFollowStateVO followUser(Long currentUserId, Long targetUserId);

    /**
     * 取消关注用户。
     *
     * @param currentUserId 当前用户编号
     * @param targetUserId 目标用户编号
     * @return 关注状态
     */
    UserFollowStateVO unfollowUser(Long currentUserId, Long targetUserId);

    /**
     * 查询粉丝列表。
     *
     * @param userId 用户编号
     * @param limit 查询条数
     * @return 粉丝列表
     */
    List<UserFollowItemVO> listFollowers(Long userId, int limit);

    /**
     * 查询关注列表。
     *
     * @param userId 用户编号
     * @param limit 查询条数
     * @return 关注列表
     */
    List<UserFollowItemVO> listFollowing(Long userId, int limit);

    /**
     * 统计粉丝数。
     *
     * @param userId 用户编号
     * @return 粉丝数量
     */
    int countFollowers(Long userId);

    /**
     * 统计关注数。
     *
     * @param userId 用户编号
     * @return 关注数量
     */
    int countFollowing(Long userId);
}
