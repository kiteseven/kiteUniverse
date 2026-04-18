package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityPostLike;

/**
 * 帖子点赞关系数据访问接口。
 */
@Mapper
public interface CommunityPostLikeMapper {

    /**
     * 查询指定帖子与用户的点赞状态。
     *
     * @param postId 帖子编号
     * @param userId 用户编号
     * @return 点赞状态，1 表示已点赞
     */
    Integer selectStatus(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 新增点赞关系。
     *
     * @param communityPostLike 点赞关系实体
     * @return 影响行数
     */
    int insert(CommunityPostLike communityPostLike);

    /**
     * 激活已有点赞关系。
     *
     * @param postId 帖子编号
     * @param userId 用户编号
     * @return 影响行数
     */
    int activate(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 取消已有点赞关系。
     *
     * @param postId 帖子编号
     * @param userId 用户编号
     * @return 影响行数
     */
    int deactivate(@Param("postId") Long postId, @Param("userId") Long userId);
}
