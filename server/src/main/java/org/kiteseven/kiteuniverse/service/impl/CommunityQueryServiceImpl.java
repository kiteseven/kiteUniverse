package org.kiteseven.kiteuniverse.service.impl;

import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.mapper.CommunityBoardMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityCommentMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityPostMapper;
import org.kiteseven.kiteuniverse.pojo.vo.community.BoardSummaryVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostCommentVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostDetailVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostSummaryVO;
import org.kiteseven.kiteuniverse.service.CommunityQueryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 社区版块、帖子和评论查询服务实现。
 */
@Service
public class CommunityQueryServiceImpl implements CommunityQueryService {

    /**
     * 默认帖子查询上限。
     */
    private static final int DEFAULT_LIMIT = 10;

    /**
     * 最大允许查询条数。
     */
    private static final int MAX_LIMIT = 20;

    private final CommunityBoardMapper communityBoardMapper;
    private final CommunityPostMapper communityPostMapper;
    private final CommunityCommentMapper communityCommentMapper;

    public CommunityQueryServiceImpl(CommunityBoardMapper communityBoardMapper,
                                     CommunityPostMapper communityPostMapper,
                                     CommunityCommentMapper communityCommentMapper) {
        this.communityBoardMapper = communityBoardMapper;
        this.communityPostMapper = communityPostMapper;
        this.communityCommentMapper = communityCommentMapper;
    }

    /**
     * 查询启用版块概要列表。
     *
     * @return 版块概要列表
     */
    @Override
    public List<BoardSummaryVO> listBoardSummaries() {
        return communityBoardMapper.selectActiveBoardSummaries(LocalDateTime.now().toLocalDate().atStartOfDay());
    }

    /**
     * 查询指定版块概要。
     *
     * @param boardId 版块编号
     * @return 版块概要
     */
    @Override
    public BoardSummaryVO getBoardSummary(Long boardId) {
        BoardSummaryVO boardSummaryVO = communityBoardMapper.selectBoardSummaryById(
                boardId,
                LocalDateTime.now().toLocalDate().atStartOfDay()
        );
        if (boardSummaryVO == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "目标版块不存在");
        }
        return boardSummaryVO;
    }

    /**
     * 查询首页精选帖子。
     *
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    @Override
    public List<PostSummaryVO> listFeaturedPosts(int limit) {
        return communityPostMapper.selectFeaturedPosts(resolveLimit(limit));
    }

    /**
     * 查询最新帖子。
     *
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    @Override
    public List<PostSummaryVO> listLatestPosts(int limit) {
        return communityPostMapper.selectLatestPosts(resolveLimit(limit));
    }

    /**
     * 查询指定版块下的帖子。
     *
     * @param boardId 版块编号
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    @Override
    public List<PostSummaryVO> listPostsByBoardId(Long boardId, int limit) {
        getBoardSummary(boardId);
        return communityPostMapper.selectPostsByBoardId(boardId, resolveLimit(limit));
    }

    /**
     * 查询指定作者发布的帖子列表。
     *
     * @param authorId 作者编号
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    @Override
    public List<PostSummaryVO> listPostsByAuthorId(Long authorId, int limit) {
        if (authorId == null || authorId <= 0L) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "作者编号不能为空");
        }
        return communityPostMapper.selectPostsByAuthorId(authorId, resolveLimit(limit));
    }

    /**
     * 查询指定用户收藏的帖子列表。
     *
     * @param userId 用户编号
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    @Override
    public List<PostSummaryVO> listFavoritePostsByUserId(Long userId, int limit) {
        if (userId == null || userId <= 0L) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户编号不能为空");
        }
        return communityPostMapper.selectFavoritePostsByUserId(userId, resolveLimit(limit));
    }

    /**
     * 查询帖子详情，并记录一次浏览。
     *
     * @param postId 帖子编号
     * @return 帖子详情
     */
    @Override
    public PostDetailVO getPostDetail(Long postId) {
        communityPostMapper.incrementViewCount(postId);
        PostDetailVO postDetailVO = communityPostMapper.selectDetailById(postId);
        if (postDetailVO == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "目标帖子不存在");
        }
        return postDetailVO;
    }

    /**
     * 查询帖子评论列表。
     *
     * @param postId 帖子编号
     * @return 评论列表
     */
    @Override
    public List<PostCommentVO> listCommentsByPostId(Long postId) {
        ensurePostExists(postId);
        return communityCommentMapper.selectByPostId(postId);
    }

    /**
     * 统计已发布帖子总数。
     *
     * @return 已发布帖子数
     */
    @Override
    public long countPublishedPosts() {
        return communityPostMapper.countPublished();
    }

    /**
     * 统计指定时间之后发布的帖子数量。
     *
     * @param publishedSince 发布时间下界
     * @return 帖子数
     */
    @Override
    public long countPublishedPostsSince(LocalDateTime publishedSince) {
        return communityPostMapper.countPublishedSince(publishedSince);
    }

    /**
     * 确认帖子存在。
     *
     * @param postId 帖子编号
     */
    private void ensurePostExists(Long postId) {
        if (postId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "帖子编号不能为空");
        }
        if (communityPostMapper.selectDetailById(postId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "目标帖子不存在");
        }
    }

    /**
     * 按关键字搜索帖子（匹配标题、摘要、徽标）。
     *
     * @param keyword 搜索关键字
     * @param limit   查询条数
     * @return 帖子概要列表
     */
    @Override
    public List<PostSummaryVO> searchPosts(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "搜索关键字不能为空");
        }
        return communityPostMapper.searchPosts(keyword.trim(), resolveLimit(limit));
    }

    /**
     * 对查询条数做安全收口。
     *
     * @param limit 原始条数
     * @return 合法条数
     */
    private int resolveLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
