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
import org.kiteseven.kiteuniverse.service.PostIndexService;
import org.kiteseven.kiteuniverse.support.redis.CachePenetrationGuardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Query-side community service for boards, posts, and comments.
 */
@Service
public class CommunityQueryServiceImpl implements CommunityQueryService {

    private static final Logger log = LoggerFactory.getLogger(CommunityQueryServiceImpl.class);

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;

    private final CommunityBoardMapper communityBoardMapper;
    private final CommunityPostMapper communityPostMapper;
    private final CommunityCommentMapper communityCommentMapper;
    private final PostIndexService postIndexService;
    private final CachePenetrationGuardService cachePenetrationGuardService;

    public CommunityQueryServiceImpl(CommunityBoardMapper communityBoardMapper,
                                     CommunityPostMapper communityPostMapper,
                                     CommunityCommentMapper communityCommentMapper,
                                     PostIndexService postIndexService,
                                     CachePenetrationGuardService cachePenetrationGuardService) {
        this.communityBoardMapper = communityBoardMapper;
        this.communityPostMapper = communityPostMapper;
        this.communityCommentMapper = communityCommentMapper;
        this.postIndexService = postIndexService;
        this.cachePenetrationGuardService = cachePenetrationGuardService;
    }

    @Override
    public List<BoardSummaryVO> listBoardSummaries() {
        return communityBoardMapper.selectActiveBoardSummaries(LocalDateTime.now().toLocalDate().atStartOfDay());
    }

    @Override
    public BoardSummaryVO getBoardSummary(Long boardId) {
        BoardSummaryVO boardSummaryVO = communityBoardMapper.selectBoardSummaryById(
                boardId,
                LocalDateTime.now().toLocalDate().atStartOfDay()
        );
        if (boardSummaryVO == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Board does not exist");
        }
        return boardSummaryVO;
    }

    @Override
    public List<PostSummaryVO> listFeaturedPosts(int limit) {
        return communityPostMapper.selectFeaturedPosts(resolveLimit(limit));
    }

    @Override
    public List<PostSummaryVO> listLatestPosts(int limit) {
        return communityPostMapper.selectLatestPosts(resolveLimit(limit));
    }

    @Override
    public List<PostSummaryVO> listPostsByBoardId(Long boardId, int limit) {
        getBoardSummary(boardId);
        return communityPostMapper.selectPostsByBoardId(boardId, resolveLimit(limit));
    }

    @Override
    public List<PostSummaryVO> listPostsByAuthorId(Long authorId, int limit) {
        if (authorId == null || authorId <= 0L) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Author id is required");
        }
        return communityPostMapper.selectPostsByAuthorId(authorId, resolveLimit(limit));
    }

    @Override
    public List<PostSummaryVO> listFavoritePostsByUserId(Long userId, int limit) {
        if (userId == null || userId <= 0L) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "User id is required");
        }
        return communityPostMapper.selectFavoritePostsByUserId(userId, resolveLimit(limit));
    }

    @Override
    public PostDetailVO getPostDetail(Long postId) {
        assertKnownPublishedPostId(postId);
        communityPostMapper.incrementViewCount(postId);
        PostDetailVO postDetailVO = communityPostMapper.selectDetailById(postId);
        if (postDetailVO == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Post does not exist");
        }
        return postDetailVO;
    }

    @Override
    public List<PostCommentVO> listCommentsByPostId(Long postId, Long userId) {
        ensurePostExists(postId);
        return communityCommentMapper.selectByPostId(postId, userId);
    }

    @Override
    public long countPublishedPosts() {
        return communityPostMapper.countPublished();
    }

    @Override
    public long countPublishedPostsSince(LocalDateTime publishedSince) {
        return communityPostMapper.countPublishedSince(publishedSince);
    }

    @Override
    public List<PostSummaryVO> searchPosts(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Search keyword must not be blank");
        }
        try {
            return postIndexService.search(keyword.trim(), resolveLimit(limit));
        } catch (Exception exception) {
            log.warn("[Search] ES unavailable, falling back to SQL LIKE: {}", exception.getMessage());
            return communityPostMapper.searchPosts(keyword.trim(), resolveLimit(limit));
        }
    }

    @Override
    public List<PostSummaryVO> listHotPosts(int limit, int days) {
        int safeDays = (days <= 0 || days > 365) ? 7 : days;
        return communityPostMapper.selectHotPosts(
                LocalDateTime.now().minusDays(safeDays),
                resolveLimit(limit)
        );
    }

    @Override
    public List<PostSummaryVO> listPostsByBoardIdPaged(Long boardId, int limit, int offset, String sort) {
        getBoardSummary(boardId);
        int safeOffset = Math.max(0, offset);
        String safeSort = (sort != null && (sort.equals("hot") || sort.equals("featured"))) ? sort : "latest";
        return communityPostMapper.selectPostsByBoardIdPaged(boardId, safeSort, resolveLimit(limit), safeOffset);
    }

    @Override
    public long countPostsByBoardId(Long boardId, String sort) {
        return communityPostMapper.countPostsByBoardId(boardId, sort);
    }

    @Override
    public List<PostSummaryVO> listPostsByBadge(String badge, int limit, int offset) {
        if (badge == null || badge.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Badge must not be blank");
        }
        int safeOffset = Math.max(0, offset);
        return communityPostMapper.selectPostsByBadge(badge.trim(), resolveLimit(limit), safeOffset);
    }

    @Override
    public List<PostSummaryVO> listRecommendedPosts(Long userId, int limit) {
        if (userId == null || userId <= 0L) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "User id is required");
        }
        List<PostSummaryVO> recommended = communityPostMapper.selectRecommendedPosts(userId, resolveLimit(limit));
        if (recommended.isEmpty()) {
            recommended = communityPostMapper.selectHotPosts(
                    LocalDateTime.now().minusDays(7),
                    resolveLimit(limit)
            );
        }
        return recommended;
    }

    private void ensurePostExists(Long postId) {
        if (postId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Post id is required");
        }
        assertKnownPublishedPostId(postId);
        if (communityPostMapper.selectDetailById(postId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Post does not exist");
        }
    }

    private void assertKnownPublishedPostId(Long postId) {
        if (postId == null || postId <= 0L) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Post id is required");
        }
        if (!cachePenetrationGuardService.mightContainPostId(postId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Post does not exist");
        }
    }

    private int resolveLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
