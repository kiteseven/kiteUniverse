package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityComment;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostCommentVO;

import java.util.List;

/**
 * 社区评论数据访问接口。
 */
@Mapper
public interface CommunityCommentMapper {

    /**
     * 统计评论总数。
     *
     * @return 评论数量
     */
    long countAll();

    /**
     * 查询指定帖子下的评论列表（可选携带当前用户的点赞状态）。
     *
     * @param postId 帖子编号
     * @param userId 当前用户编号（可为 null）
     * @return 评论列表
     */
    List<PostCommentVO> selectByPostId(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 按编号查询评论实体。
     *
     * @param id 评论编号
     * @return 评论实体
     */
    CommunityComment selectById(@Param("id") Long id);

    /**
     * 新增评论记录。
     *
     * @param communityComment 评论实体
     * @return 影响行数
     */
    int insert(CommunityComment communityComment);

    /**
     * 增加评论点赞数。
     *
     * @param id 评论编号
     * @return 影响行数
     */
    int incrementLikeCount(@Param("id") Long id);

    /**
     * 减少评论点赞数。
     *
     * @param id 评论编号
     * @return 影响行数
     */
    int decrementLikeCount(@Param("id") Long id);
}
