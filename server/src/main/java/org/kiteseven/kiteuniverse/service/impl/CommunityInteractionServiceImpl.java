package org.kiteseven.kiteuniverse.service.impl;

import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.mapper.CommunityBoardMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityCommentLikeMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityCommentMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityPostFavoriteMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityPostLikeMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityPostMapper;
import org.kiteseven.kiteuniverse.mapper.UserMapper;
import org.kiteseven.kiteuniverse.pojo.dto.community.PostCommentCreateDTO;
import org.kiteseven.kiteuniverse.pojo.dto.community.PostCreateDTO;
import org.kiteseven.kiteuniverse.pojo.dto.community.PostUpdateDTO;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityBoard;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityComment;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityCommentLike;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityPost;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityPostFavorite;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityPostLike;
import org.kiteseven.kiteuniverse.pojo.entity.User;
import org.kiteseven.kiteuniverse.pojo.vo.community.CommentLikeStateVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostCommentVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostDetailVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostFavoriteStateVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostLikeStateVO;
import org.kiteseven.kiteuniverse.service.CheckInService;
import org.kiteseven.kiteuniverse.service.CommunityInteractionService;
import org.kiteseven.kiteuniverse.service.NotificationService;
import org.kiteseven.kiteuniverse.service.PostIndexService;
import org.kiteseven.kiteuniverse.support.cache.TwoLevelCache;
import org.kiteseven.kiteuniverse.support.community.CommunityContentCacheKeys;
import org.kiteseven.kiteuniverse.support.redis.CachePenetrationGuardService;
import org.kiteseven.kiteuniverse.support.redis.DistributedLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * Handles post, comment, favorite, and like mutations for the community module.
 */
@Service
public class CommunityInteractionServiceImpl implements CommunityInteractionService {

    private static final Logger log = LoggerFactory.getLogger(CommunityInteractionServiceImpl.class);

    private static final int STATUS_ENABLED = 1;
    private static final int STATUS_DISABLED = 0;

    private final CommunityBoardMapper communityBoardMapper;
    private final CommunityPostMapper communityPostMapper;
    private final CommunityPostFavoriteMapper communityPostFavoriteMapper;
    private final CommunityPostLikeMapper communityPostLikeMapper;
    private final CommunityCommentMapper communityCommentMapper;
    private final CommunityCommentLikeMapper communityCommentLikeMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final CheckInService checkInService;
    private final PostIndexService postIndexService;
    private final CachePenetrationGuardService cachePenetrationGuardService;
    private final DistributedLockService distributedLockService;

    public CommunityInteractionServiceImpl(CommunityBoardMapper communityBoardMapper,
                                           CommunityPostMapper communityPostMapper,
                                           CommunityPostFavoriteMapper communityPostFavoriteMapper,
                                           CommunityPostLikeMapper communityPostLikeMapper,
                                           CommunityCommentMapper communityCommentMapper,
                                           CommunityCommentLikeMapper communityCommentLikeMapper,
                                           UserMapper userMapper,
                                           NotificationService notificationService,
                                           CheckInService checkInService,
                                           PostIndexService postIndexService,
                                           CachePenetrationGuardService cachePenetrationGuardService,
                                           DistributedLockService distributedLockService) {
        this.communityBoardMapper = communityBoardMapper;
        this.communityPostMapper = communityPostMapper;
        this.communityPostFavoriteMapper = communityPostFavoriteMapper;
        this.communityPostLikeMapper = communityPostLikeMapper;
        this.communityCommentMapper = communityCommentMapper;
        this.communityCommentLikeMapper = communityCommentLikeMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
        this.checkInService = checkInService;
        this.postIndexService = postIndexService;
        this.cachePenetrationGuardService = cachePenetrationGuardService;
        this.distributedLockService = distributedLockService;
    }

    @Override
    @TwoLevelCache(mode = TwoLevelCache.Mode.EVICT,
            evictKeys = {CommunityContentCacheKeys.HOME_PAGE, CommunityContentCacheKeys.BOARDS_PAGE})
    @Transactional(rollbackFor = Exception.class)
    public PostDetailVO createPost(Long authorId, PostCreateDTO postCreateDTO) {
        validatePostPayload(
                postCreateDTO == null ? null : postCreateDTO.getBoardId(),
                postCreateDTO == null ? null : postCreateDTO.getTitle(),
                postCreateDTO == null ? null : postCreateDTO.getSummary(),
                postCreateDTO == null ? null : postCreateDTO.getContent()
        );
        User author = getExistingUser(authorId);
        CommunityBoard communityBoard = getExistingBoard(postCreateDTO.getBoardId());

        CommunityPost communityPost = new CommunityPost();
        communityPost.setBoardId(communityBoard.getId());
        communityPost.setAuthorId(author.getId());
        communityPost.setTitle(postCreateDTO.getTitle().trim());
        communityPost.setSummary(postCreateDTO.getSummary().trim());
        communityPost.setContent(postCreateDTO.getContent().trim());
        communityPost.setBadge(normalizeText(postCreateDTO.getBadge()));
        communityPost.setStatus(STATUS_ENABLED);
        communityPost.setFeatured(0);
        communityPost.setPinned(0);
        communityPost.setViewCount(0);
        communityPost.setCommentCount(0);
        communityPost.setFavoriteCount(0);
        communityPost.setLikeCount(0);
        communityPost.setIsAiGenerated(Boolean.TRUE.equals(postCreateDTO.getIsAiGenerated()) ? 1 : 0);
        communityPost.setGalleryImages(postCreateDTO.getGalleryImages());
        communityPost.setPublishedAt(LocalDateTime.now());
        communityPostMapper.insert(communityPost);

        cachePenetrationGuardService.addPostId(communityPost.getId());
        checkInService.addPoints(authorId, 5);
        checkInService.grantBadge(authorId, "FIRST_POST");
        syncPostIndex(communityPost.getId());
        return communityPostMapper.selectDetailById(communityPost.getId());
    }

    @Override
    public PostDetailVO getManagePost(Long authorId, Long postId) {
        CommunityPost communityPost = getOwnedPost(authorId, postId);
        PostDetailVO postDetailVO = communityPostMapper.selectDetailById(communityPost.getId());
        if (postDetailVO == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Post does not exist");
        }
        return postDetailVO;
    }

    @Override
    @TwoLevelCache(mode = TwoLevelCache.Mode.EVICT,
            evictKeys = {CommunityContentCacheKeys.HOME_PAGE, CommunityContentCacheKeys.BOARDS_PAGE})
    @Transactional(rollbackFor = Exception.class)
    public PostDetailVO updatePost(Long authorId, Long postId, PostUpdateDTO postUpdateDTO) {
        validatePostPayload(
                postUpdateDTO == null ? null : postUpdateDTO.getBoardId(),
                postUpdateDTO == null ? null : postUpdateDTO.getTitle(),
                postUpdateDTO == null ? null : postUpdateDTO.getSummary(),
                postUpdateDTO == null ? null : postUpdateDTO.getContent()
        );
        CommunityPost existingPost = getOwnedPost(authorId, postId);
        CommunityBoard communityBoard = getExistingBoard(postUpdateDTO.getBoardId());

        existingPost.setBoardId(communityBoard.getId());
        existingPost.setTitle(postUpdateDTO.getTitle().trim());
        existingPost.setSummary(postUpdateDTO.getSummary().trim());
        existingPost.setContent(postUpdateDTO.getContent().trim());
        existingPost.setBadge(normalizeText(postUpdateDTO.getBadge()));
        existingPost.setIsAiGenerated(Boolean.TRUE.equals(postUpdateDTO.getIsAiGenerated()) ? 1 : 0);
        existingPost.setGalleryImages(postUpdateDTO.getGalleryImages());
        communityPostMapper.updatePost(existingPost);

        syncPostIndex(postId);
        return communityPostMapper.selectDetailById(postId);
    }

    @Override
    @TwoLevelCache(mode = TwoLevelCache.Mode.EVICT,
            evictKeys = {CommunityContentCacheKeys.HOME_PAGE, CommunityContentCacheKeys.BOARDS_PAGE})
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long authorId, Long postId) {
        getOwnedPost(authorId, postId);
        int affectedRows = communityPostMapper.softDeleteByIdAndAuthorId(postId, authorId);
        if (affectedRows <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "Failed to delete post");
        }
        removePostIndex(postId);
    }

    @Override
    @TwoLevelCache(mode = TwoLevelCache.Mode.EVICT,
            evictKeys = {CommunityContentCacheKeys.HOME_PAGE, CommunityContentCacheKeys.BOARDS_PAGE})
    @Transactional(rollbackFor = Exception.class)
    public PostCommentVO createComment(Long authorId, Long postId, PostCommentCreateDTO postCommentCreateDTO) {
        validateCommentRequest(postCommentCreateDTO);
        User author = getExistingUser(authorId);
        getPublishedPost(postId);

        CommunityComment communityComment = new CommunityComment();
        communityComment.setPostId(postId);
        communityComment.setParentId(postCommentCreateDTO.getParentId());
        communityComment.setAuthorId(author.getId());
        communityComment.setContent(postCommentCreateDTO.getContent().trim());
        communityComment.setStatus(STATUS_ENABLED);
        communityCommentMapper.insert(communityComment);
        communityPostMapper.incrementCommentCount(postId);
        checkInService.addPoints(authorId, 2);

        CommunityPost commentedPost = communityPostMapper.selectById(postId);
        if (commentedPost != null && commentedPost.getAuthorId() != null) {
            notificationService.createCommentNotification(
                    authorId,
                    postId,
                    commentedPost.getAuthorId(),
                    communityComment.getId(),
                    communityComment.getContent()
            );
        }

        return communityCommentMapper.selectByPostId(postId, null).stream()
                .filter(comment -> comment.getId() != null && comment.getId().equals(communityComment.getId()))
                .findFirst()
                .orElseGet(() -> {
                    PostCommentVO postCommentVO = new PostCommentVO();
                    postCommentVO.setId(communityComment.getId());
                    postCommentVO.setPostId(postId);
                    postCommentVO.setAuthorId(author.getId());
                    postCommentVO.setAuthorName(StringUtils.hasText(author.getNickname())
                            ? author.getNickname()
                            : author.getUsername());
                    postCommentVO.setAuthorAvatar(author.getAvatar());
                    postCommentVO.setContent(communityComment.getContent());
                    postCommentVO.setLiked(false);
                    postCommentVO.setCreateTime(communityComment.getCreateTime());
                    return postCommentVO;
                });
    }

    @Override
    public PostFavoriteStateVO getFavoriteState(Long userId, Long postId) {
        getExistingUser(userId);
        CommunityPost communityPost = getPublishedPost(postId);
        Integer favoriteStatus = communityPostFavoriteMapper.selectStatus(postId, userId);
        return buildFavoriteState(
                communityPost.getId(),
                favoriteStatus != null && favoriteStatus == STATUS_ENABLED,
                communityPost.getFavoriteCount()
        );
    }

    @Override
    @TwoLevelCache(mode = TwoLevelCache.Mode.EVICT,
            evictKeys = {CommunityContentCacheKeys.HOME_PAGE, CommunityContentCacheKeys.BOARDS_PAGE})
    @Transactional(rollbackFor = Exception.class)
    public PostFavoriteStateVO favoritePost(Long userId, Long postId) {
        return distributedLockService.executeWithLock(buildPostFavoriteLockKey(postId, userId), () -> {
            getExistingUser(userId);
            CommunityPost communityPost = getPublishedPost(postId);
            Integer favoriteStatus = communityPostFavoriteMapper.selectStatus(postId, userId);
            if (favoriteStatus == null) {
                CommunityPostFavorite communityPostFavorite = new CommunityPostFavorite();
                communityPostFavorite.setPostId(postId);
                communityPostFavorite.setUserId(userId);
                communityPostFavorite.setStatus(STATUS_ENABLED);
                communityPostFavoriteMapper.insert(communityPostFavorite);
                communityPostMapper.incrementFavoriteCount(postId);
                if (communityPost.getAuthorId() != null) {
                    checkInService.addPoints(communityPost.getAuthorId(), 5);
                }
            } else if (favoriteStatus == STATUS_DISABLED) {
                communityPostFavoriteMapper.activate(postId, userId);
                communityPostMapper.incrementFavoriteCount(postId);
                if (communityPost.getAuthorId() != null) {
                    checkInService.addPoints(communityPost.getAuthorId(), 5);
                }
            }

            CommunityPost refreshedPost = getPublishedPost(postId);
            return buildFavoriteState(postId, true, refreshedPost.getFavoriteCount());
        });
    }

    @Override
    @TwoLevelCache(mode = TwoLevelCache.Mode.EVICT,
            evictKeys = {CommunityContentCacheKeys.HOME_PAGE, CommunityContentCacheKeys.BOARDS_PAGE})
    @Transactional(rollbackFor = Exception.class)
    public PostFavoriteStateVO unfavoritePost(Long userId, Long postId) {
        return distributedLockService.executeWithLock(buildPostFavoriteLockKey(postId, userId), () -> {
            getExistingUser(userId);
            CommunityPost communityPost = getPublishedPost(postId);
            Integer favoriteStatus = communityPostFavoriteMapper.selectStatus(postId, userId);
            if (favoriteStatus != null && favoriteStatus == STATUS_ENABLED) {
                communityPostFavoriteMapper.deactivate(postId, userId);
                communityPostMapper.decrementFavoriteCount(postId);
                communityPost = getPublishedPost(postId);
            }

            return buildFavoriteState(postId, false, communityPost.getFavoriteCount());
        });
    }

    @Override
    public PostLikeStateVO getLikeState(Long userId, Long postId) {
        getExistingUser(userId);
        CommunityPost communityPost = getPublishedPost(postId);
        Integer likeStatus = communityPostLikeMapper.selectStatus(postId, userId);
        return buildLikeState(
                communityPost.getId(),
                likeStatus != null && likeStatus == STATUS_ENABLED,
                communityPost.getLikeCount()
        );
    }

    @Override
    @TwoLevelCache(mode = TwoLevelCache.Mode.EVICT,
            evictKeys = {CommunityContentCacheKeys.HOME_PAGE, CommunityContentCacheKeys.BOARDS_PAGE})
    @Transactional(rollbackFor = Exception.class)
    public PostLikeStateVO likePost(Long userId, Long postId) {
        return distributedLockService.executeWithLock(buildPostLikeLockKey(postId, userId), () -> {
            getExistingUser(userId);
            CommunityPost post = getPublishedPost(postId);
            Integer likeStatus = communityPostLikeMapper.selectStatus(postId, userId);
            boolean newLike = false;
            if (likeStatus == null) {
                CommunityPostLike communityPostLike = new CommunityPostLike();
                communityPostLike.setPostId(postId);
                communityPostLike.setUserId(userId);
                communityPostLike.setStatus(STATUS_ENABLED);
                communityPostLikeMapper.insert(communityPostLike);
                communityPostMapper.incrementLikeCount(postId);
                newLike = true;
            } else if (likeStatus == STATUS_DISABLED) {
                communityPostLikeMapper.activate(postId, userId);
                communityPostMapper.incrementLikeCount(postId);
                newLike = true;
            }

            if (newLike && post.getAuthorId() != null) {
                notificationService.createPostLikeNotification(userId, postId, post.getAuthorId());
                checkInService.addPoints(post.getAuthorId(), 3);
            }

            CommunityPost refreshedPost = getPublishedPost(postId);
            return buildLikeState(postId, true, refreshedPost.getLikeCount());
        });
    }

    @Override
    @TwoLevelCache(mode = TwoLevelCache.Mode.EVICT,
            evictKeys = {CommunityContentCacheKeys.HOME_PAGE, CommunityContentCacheKeys.BOARDS_PAGE})
    @Transactional(rollbackFor = Exception.class)
    public PostLikeStateVO unlikePost(Long userId, Long postId) {
        return distributedLockService.executeWithLock(buildPostLikeLockKey(postId, userId), () -> {
            getExistingUser(userId);
            CommunityPost communityPost = getPublishedPost(postId);
            Integer likeStatus = communityPostLikeMapper.selectStatus(postId, userId);
            if (likeStatus != null && likeStatus == STATUS_ENABLED) {
                communityPostLikeMapper.deactivate(postId, userId);
                communityPostMapper.decrementLikeCount(postId);
                communityPost = getPublishedPost(postId);
            }

            return buildLikeState(postId, false, communityPost.getLikeCount());
        });
    }

    @Override
    public CommentLikeStateVO getCommentLikeState(Long userId, Long commentId) {
        getExistingUser(userId);
        CommunityComment comment = getPublishedComment(commentId);
        Integer likeStatus = communityCommentLikeMapper.selectStatus(commentId, userId);
        return buildCommentLikeState(
                comment.getId(),
                likeStatus != null && likeStatus == STATUS_ENABLED,
                comment.getLikeCount()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentLikeStateVO likeComment(Long userId, Long commentId) {
        return distributedLockService.executeWithLock(buildCommentLikeLockKey(commentId, userId), () -> {
            getExistingUser(userId);
            CommunityComment comment = getPublishedComment(commentId);
            Integer likeStatus = communityCommentLikeMapper.selectStatus(commentId, userId);
            boolean newLike = false;
            if (likeStatus == null) {
                CommunityCommentLike communityCommentLike = new CommunityCommentLike();
                communityCommentLike.setCommentId(commentId);
                communityCommentLike.setUserId(userId);
                communityCommentLike.setStatus(STATUS_ENABLED);
                communityCommentLikeMapper.insert(communityCommentLike);
                communityCommentMapper.incrementLikeCount(commentId);
                newLike = true;
            } else if (likeStatus == STATUS_DISABLED) {
                communityCommentLikeMapper.activate(commentId, userId);
                communityCommentMapper.incrementLikeCount(commentId);
                newLike = true;
            }

            if (newLike && comment.getAuthorId() != null) {
                notificationService.createCommentLikeNotification(
                        userId,
                        commentId,
                        comment.getAuthorId(),
                        comment.getPostId()
                );
            }

            CommunityComment refreshedComment = getPublishedComment(commentId);
            return buildCommentLikeState(commentId, true, refreshedComment.getLikeCount());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentLikeStateVO unlikeComment(Long userId, Long commentId) {
        return distributedLockService.executeWithLock(buildCommentLikeLockKey(commentId, userId), () -> {
            getExistingUser(userId);
            CommunityComment comment = getPublishedComment(commentId);
            Integer likeStatus = communityCommentLikeMapper.selectStatus(commentId, userId);
            if (likeStatus != null && likeStatus == STATUS_ENABLED) {
                communityCommentLikeMapper.deactivate(commentId, userId);
                communityCommentMapper.decrementLikeCount(commentId);
                comment = getPublishedComment(commentId);
            }

            return buildCommentLikeState(commentId, false, comment.getLikeCount());
        });
    }

    private void validatePostPayload(Long boardId, String title, String summary, String content) {
        if (boardId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Please choose a board");
        }
        if (!StringUtils.hasText(title)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Post title must not be blank");
        }
        if (!StringUtils.hasText(summary)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Post summary must not be blank");
        }
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Post content must not be blank");
        }
        if (title.trim().length() > 120) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Post title must be 120 characters or fewer");
        }
        if (summary.trim().length() > 255) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Post summary must be 255 characters or fewer");
        }
    }

    private void validateCommentRequest(PostCommentCreateDTO postCommentCreateDTO) {
        if (postCommentCreateDTO == null || !StringUtils.hasText(postCommentCreateDTO.getContent())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Comment content must not be blank");
        }
        if (postCommentCreateDTO.getContent().trim().length() > 1000) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Comment content must be 1000 characters or fewer");
        }
    }

    private User getExistingUser(Long userId) {
        if (userId != null && userId > 0L && !cachePenetrationGuardService.mightContainUserId(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Current user does not exist");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Current user does not exist");
        }
        return user;
    }

    private CommunityPost getPublishedPost(Long postId) {
        if (postId != null && postId > 0L && !cachePenetrationGuardService.mightContainPostId(postId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Post does not exist");
        }

        CommunityPost communityPost = communityPostMapper.selectById(postId);
        if (communityPost == null || communityPost.getStatus() == null || communityPost.getStatus() != STATUS_ENABLED) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Post does not exist");
        }
        return communityPost;
    }

    private CommunityPost getOwnedPost(Long authorId, Long postId) {
        CommunityPost communityPost = getPublishedPost(postId);
        if (communityPost.getAuthorId() == null || !communityPost.getAuthorId().equals(authorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Only the author can manage this post");
        }
        return communityPost;
    }

    private CommunityBoard getExistingBoard(Long boardId) {
        CommunityBoard communityBoard = communityBoardMapper.selectById(boardId);
        if (communityBoard == null || communityBoard.getStatus() == null || communityBoard.getStatus() != STATUS_ENABLED) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Board does not exist");
        }
        return communityBoard;
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private CommunityComment getPublishedComment(Long commentId) {
        CommunityComment comment = communityCommentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() == null || comment.getStatus() != STATUS_ENABLED) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Comment does not exist");
        }
        return comment;
    }

    private PostLikeStateVO buildLikeState(Long postId, boolean liked, Integer likeCount) {
        PostLikeStateVO postLikeStateVO = new PostLikeStateVO();
        postLikeStateVO.setPostId(postId);
        postLikeStateVO.setLiked(liked);
        postLikeStateVO.setLikeCount(likeCount == null ? 0 : likeCount);
        return postLikeStateVO;
    }

    private CommentLikeStateVO buildCommentLikeState(Long commentId, boolean liked, Integer likeCount) {
        CommentLikeStateVO commentLikeStateVO = new CommentLikeStateVO();
        commentLikeStateVO.setCommentId(commentId);
        commentLikeStateVO.setLiked(liked);
        commentLikeStateVO.setLikeCount(likeCount == null ? 0 : likeCount);
        return commentLikeStateVO;
    }

    private PostFavoriteStateVO buildFavoriteState(Long postId, boolean favorited, Integer favoriteCount) {
        PostFavoriteStateVO postFavoriteStateVO = new PostFavoriteStateVO();
        postFavoriteStateVO.setPostId(postId);
        postFavoriteStateVO.setFavorited(favorited);
        postFavoriteStateVO.setFavoriteCount(favoriteCount == null ? 0 : favoriteCount);
        return postFavoriteStateVO;
    }

    private void syncPostIndex(Long postId) {
        try {
            postIndexService.indexPost(postId);
        } catch (Exception exception) {
            log.warn("[ES] Failed to index post {}: {}", postId, exception.getMessage());
        }
    }

    private void removePostIndex(Long postId) {
        try {
            postIndexService.removePost(postId);
        } catch (Exception exception) {
            log.warn("[ES] Failed to remove post {} from index: {}", postId, exception.getMessage());
        }
    }

    private String buildPostFavoriteLockKey(Long postId, Long userId) {
        return "community:post-favorite:post:" + postId + ":user:" + userId;
    }

    private String buildPostLikeLockKey(Long postId, Long userId) {
        return "community:post-like:post:" + postId + ":user:" + userId;
    }

    private String buildCommentLikeLockKey(Long commentId, Long userId) {
        return "community:comment-like:comment:" + commentId + ":user:" + userId;
    }
}
