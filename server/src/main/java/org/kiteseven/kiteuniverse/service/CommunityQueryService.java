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
     * 查询帖子评论列表。
     *
     * @param postId 帖子编号
     * @return 评论列表
     */
    List<PostCommentVO> listCommentsByPostId(Long postId);

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
}
