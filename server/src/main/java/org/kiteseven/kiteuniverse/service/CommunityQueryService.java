package org.kiteseven.kiteuniverse.service;

import org.kiteseven.kiteuniverse.pojo.vo.community.BoardSummaryVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostCommentVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostDetailVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostSummaryVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提供社区版块与帖子查询能力。
 */
public interface CommunityQueryService {

    /**
     * 查询启用版块概要列表。
     *
     * @return 版块概要列表
     */
    List<BoardSummaryVO> listBoardSummaries();

    /**
     * 查询指定版块概要。
     *
     * @param boardId 版块编号
     * @return 版块概要
     */
    BoardSummaryVO getBoardSummary(Long boardId);

    /**
     * 查询首页精选帖子。
     *
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    List<PostSummaryVO> listFeaturedPosts(int limit);

    /**
     * 查询最新帖子。
     *
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    List<PostSummaryVO> listLatestPosts(int limit);

    /**
     * 查询指定版块下的帖子。
     *
     * @param boardId 版块编号
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    List<PostSummaryVO> listPostsByBoardId(Long boardId, int limit);

    /**
     * 查询指定作者发布的帖子。
     *
     * @param authorId 作者编号
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    List<PostSummaryVO> listPostsByAuthorId(Long authorId, int limit);

    /**
     * 查询指定用户收藏的帖子。
     *
     * @param userId 用户编号
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    List<PostSummaryVO> listFavoritePostsByUserId(Long userId, int limit);

    /**
     * 查询帖子详情。
     *
     * @param postId 帖子编号
     * @return 帖子详情
     */
    PostDetailVO getPostDetail(Long postId);

    /**
     * 查询帖子评论列表（可选携带当前用户的点赞状态）。
     *
     * @param postId 帖子编号
     * @param userId 当前用户编号（可为 null）
     * @return 评论列表
     */
    List<PostCommentVO> listCommentsByPostId(Long postId, Long userId);

    /**
     * 统计已发布帖子总数。
     *
     * @return 已发布帖子数
     */
    long countPublishedPosts();

    /**
     * 统计指定时间之后发布的帖子数量。
     *
     * @param publishedSince 发布时间下界
     * @return 帖子数
     */
    long countPublishedPostsSince(LocalDateTime publishedSince);

    /**
     * 按关键字搜索帖子（匹配标题、摘要、徽标）。
     *
     * @param keyword 搜索关键字
     * @param limit   查询条数
     * @return 帖子概要列表
     */
    List<PostSummaryVO> searchPosts(String keyword, int limit);

    /**
     * 查询热门帖子。
     *
     * @param limit 查询条数
     * @param days  时间窗口（天数）
     * @return 帖子概要列表
     */
    List<PostSummaryVO> listHotPosts(int limit, int days);

    /**
     * 查询指定版块帖子（支持排序和分页）。
     *
     * @param boardId 版块编号
     * @param limit   每页条数
     * @param offset  偏移量
     * @param sort    排序方式：latest / hot / featured
     * @return 帖子概要列表
     */
    List<PostSummaryVO> listPostsByBoardIdPaged(Long boardId, int limit, int offset, String sort);

    /**
     * 统计指定版块帖子总数。
     *
     * @param boardId 版块编号
     * @param sort    排序/筛选方式
     * @return 帖子总数
     */
    long countPostsByBoardId(Long boardId, String sort);

    /**
     * 按徽标查询帖子列表（话题聚合）。
     *
     * @param badge  徽标文案
     * @param limit  每页条数
     * @param offset 偏移量
     * @return 帖子概要列表
     */
    List<PostSummaryVO> listPostsByBadge(String badge, int limit, int offset);

    /**
     * 查询个性化推荐帖子（基于用户互动历史的版块热门）。
     *
     * @param userId 用户编号
     * @param limit  查询条数
     * @return 帖子概要列表
     */
    List<PostSummaryVO> listRecommendedPosts(Long userId, int limit);
}
