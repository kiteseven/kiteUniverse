package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityCommentLike;

/**
 * 评论点赞关系数据访问接口。
 */
@Mapper
public interface CommunityCommentLikeMapper {

    /**
     * 查询指定评论与用户的点赞状态。
     *
     * @param commentId 评论编号
     * @param userId 用户编号
     * @return 点赞状态，1 表示已点赞
     */
    Integer selectStatus(@Param("commentId") Long commentId, @Param("userId") Long userId);

    /**
     * 新增点赞关系。
     *
     * @param communityCommentLike 点赞关系实体
     * @return 影响行数
     */
    int insert(CommunityCommentLike communityCommentLike);

    /**
     * 激活已有点赞关系。
     *
     * @param commentId 评论编号
     * @param userId 用户编号
     * @return 影响行数
     */
    int activate(@Param("commentId") Long commentId, @Param("userId") Long userId);

    /**
     * 取消已有点赞关系。
     *
     * @param commentId 评论编号
     * @param userId 用户编号
     * @return 影响行数
     */
    int deactivate(@Param("commentId") Long commentId, @Param("userId") Long userId);
}
