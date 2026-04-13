package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityPostFavorite;

/**
 * 帖子收藏关系数据访问接口。
 */
@Mapper
public interface CommunityPostFavoriteMapper {

    /**
     * 查询指定帖子与用户的收藏状态。
     *
     * @param postId 帖子编号
     * @param userId 用户编号
     * @return 收藏状态，1 表示已收藏
     */
    Integer selectStatus(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 新增收藏关系。
     *
     * @param communityPostFavorite 收藏关系实体
     * @return 影响行数
     */
    int insert(CommunityPostFavorite communityPostFavorite);

    /**
     * 激活已有收藏关系。
     *
     * @param postId 帖子编号
     * @param userId 用户编号
     * @return 影响行数
     */
    int activate(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 取消已有收藏关系。
     *
     * @param postId 帖子编号
     * @param userId 用户编号
     * @return 影响行数
     */
    int deactivate(@Param("postId") Long postId, @Param("userId") Long userId);
}
