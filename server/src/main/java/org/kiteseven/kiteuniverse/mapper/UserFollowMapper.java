package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.UserFollow;
import org.kiteseven.kiteuniverse.pojo.vo.user.UserFollowItemVO;

import java.util.List;

/**
 * 用户关注关系数据访问接口。
 */
@Mapper
public interface UserFollowMapper {

    /**
     * 查询指定关注者与被关注者的关注状态。
     *
     * @param followerId 关注者编号
     * @param followingId 被关注者编号
     * @return 关注状态，1 表示已关注
     */
    Integer selectStatus(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /**
     * 新增关注关系。
     *
     * @param userFollow 关注关系实体
     * @return 影响行数
     */
    int insert(UserFollow userFollow);

    /**
     * 激活已有关注关系。
     *
     * @param followerId 关注者编号
     * @param followingId 被关注者编号
     * @return 影响行数
     */
    int activate(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /**
     * 取消已有关注关系。
     *
     * @param followerId 关注者编号
     * @param followingId 被关注者编号
     * @return 影响行数
     */
    int deactivate(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /**
     * 统计粉丝数。
     *
     * @param userId 用户编号
     * @return 粉丝数量
     */
    int countFollowers(@Param("userId") Long userId);

    /**
     * 统计关注数。
     *
     * @param userId 用户编号
     * @return 关注数量
     */
    int countFollowing(@Param("userId") Long userId);

    /**
     * 查询粉丝列表。
     *
     * @param userId 用户编号
     * @param limit  查询条数
     * @return 粉丝列表
     */
    List<UserFollowItemVO> selectFollowers(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 查询关注列表。
     *
     * @param userId 用户编号
     * @param limit  查询条数
     * @return 关注列表
     */
    List<UserFollowItemVO> selectFollowing(@Param("userId") Long userId, @Param("limit") int limit);
}
