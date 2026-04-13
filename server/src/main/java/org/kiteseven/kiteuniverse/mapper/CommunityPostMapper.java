package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityPost;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostDetailVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostSummaryVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 社区帖子数据访问接口。
 */
@Mapper
public interface CommunityPostMapper {

    /**
     * 统计帖子总数。
     *
     * @return 帖子数量
     */
    long countAll();

    /**
     * 统计已发布帖子数量。
     *
     * @return 已发布帖子数量
     */
    long countPublished();

    /**
     * 统计指定时间之后发布的帖子数量。
     *
     * @param publishedSince 发布时间下界
     * @return 帖子数量
     */
    long countPublishedSince(@Param("publishedSince") LocalDateTime publishedSince);

    /**
     * 按编号查询帖子实体。
     *
     * @param id 帖子编号
     * @return 帖子实体
     */
    CommunityPost selectById(@Param("id") Long id);

    /**
     * 按编号查询帖子详情。
     *
     * @param id 帖子编号
     * @return 帖子详情
     */
    PostDetailVO selectDetailById(@Param("id") Long id);

    /**
     * 查询首页精选帖子。
     *
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    List<PostSummaryVO> selectFeaturedPosts(@Param("limit") int limit);

    /**
     * 查询最新帖子。
     *
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    List<PostSummaryVO> selectLatestPosts(@Param("limit") int limit);

    /**
     * 查询指定版块下的帖子列表。
     *
     * @param boardId 版块编号
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    List<PostSummaryVO> selectPostsByBoardId(@Param("boardId") Long boardId, @Param("limit") int limit);

    /**
     * 查询指定作者发布的帖子列表。
     *
     * @param authorId 作者编号
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    List<PostSummaryVO> selectPostsByAuthorId(@Param("authorId") Long authorId, @Param("limit") int limit);

    /**
     * 查询指定用户收藏的帖子列表。
     *
     * @param userId 用户编号
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    List<PostSummaryVO> selectFavoritePostsByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 新增帖子记录。
     *
     * @param communityPost 帖子实体
     * @return 影响行数
     */
    int insert(CommunityPost communityPost);

    /**
     * 更新帖子主体内容。
     *
     * @param communityPost 帖子实体
     * @return 影响行数
     */
    int updatePost(CommunityPost communityPost);

    /**
     * 软删除指定作者名下的帖子。
     *
     * @param id 帖子编号
     * @param authorId 作者编号
     * @return 影响行数
     */
    int softDeleteByIdAndAuthorId(@Param("id") Long id, @Param("authorId") Long authorId);

    /**
     * 增加帖子浏览量。
     *
     * @param id 帖子编号
     * @return 影响行数
     */
    int incrementViewCount(@Param("id") Long id);

    /**
     * 增加帖子评论数。
     *
     * @param id 帖子编号
     * @return 影响行数
     */
    int incrementCommentCount(@Param("id") Long id);

    /**
     * 增加帖子收藏数。
     *
     * @param id 帖子编号
     * @return 影响行数
     */
    int incrementFavoriteCount(@Param("id") Long id);

    /**
     * 减少帖子收藏数。
     *
     * @param id 帖子编号
     * @return 影响行数
     */
    int decrementFavoriteCount(@Param("id") Long id);

    /**
     * 按关键字搜索帖子（匹配标题、摘要、徽标）。
     *
     * @param keyword 搜索关键字
     * @param limit   查询条数
     * @return 帖子概要列表
     */
    List<PostSummaryVO> searchPosts(@Param("keyword") String keyword, @Param("limit") int limit);

    /**
     * 查询热门帖子（按浏览×1 + 评论×5 + 收藏×3 综合热度降序排列）。
     *
     * @param publishedSince 发布时间下界（用于限定时间窗口）
     * @param limit          查询条数
     * @return 帖子概要列表
     */
    List<PostSummaryVO> selectHotPosts(@Param("publishedSince") java.time.LocalDateTime publishedSince,
                                       @Param("limit") int limit);
}
