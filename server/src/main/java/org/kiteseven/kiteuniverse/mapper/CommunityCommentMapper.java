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
     * 查询指定帖子下的评论列表。
     *
     * @param postId 帖子编号
     * @return 评论列表
     */
    List<PostCommentVO> selectByPostId(@Param("postId") Long postId);

    /**
     * 新增评论记录。
     *
     * @param communityComment 评论实体
     * @return 影响行数
     */
    int insert(CommunityComment communityComment);
}
