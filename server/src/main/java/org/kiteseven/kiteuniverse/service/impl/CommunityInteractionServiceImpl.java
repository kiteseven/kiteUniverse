package org.kiteseven.kiteuniverse.service.impl;

import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.mapper.CommunityBoardMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityCommentMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityPostFavoriteMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityPostMapper;
import org.kiteseven.kiteuniverse.mapper.UserMapper;
import org.kiteseven.kiteuniverse.pojo.dto.community.PostCommentCreateDTO;
import org.kiteseven.kiteuniverse.pojo.dto.community.PostCreateDTO;
import org.kiteseven.kiteuniverse.pojo.dto.community.PostUpdateDTO;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityBoard;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityComment;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityPost;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityPostFavorite;
import org.kiteseven.kiteuniverse.pojo.entity.User;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostCommentVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostDetailVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostFavoriteStateVO;
import org.kiteseven.kiteuniverse.service.CommunityInteractionService;
import org.kiteseven.kiteuniverse.support.community.CommunityContentCacheKeys;
import org.kiteseven.kiteuniverse.support.redis.RedisKeyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 社区互动服务实现，负责发帖和评论。
 */
@Service
public class CommunityInteractionServiceImpl implements CommunityInteractionService {

    /**
     * Logger used for graceful cache degradation when Redis is unavailable.
     */
    private static final Logger log = LoggerFactory.getLogger(CommunityInteractionServiceImpl.class);

    /**
     * 默认启用状态。
     */
    private static final int STATUS_ENABLED = 1;

    /**
     * 默认停用状态。
     */
    private static final int STATUS_DISABLED = 0;

    private final CommunityBoardMapper communityBoardMapper;
    private final CommunityPostMapper communityPostMapper;
    private final CommunityPostFavoriteMapper communityPostFavoriteMapper;
    private final CommunityCommentMapper communityCommentMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisKeyManager redisKeyManager;

    public CommunityInteractionServiceImpl(CommunityBoardMapper communityBoardMapper,
                                           CommunityPostMapper communityPostMapper,
                                           CommunityPostFavoriteMapper communityPostFavoriteMapper,
                                           CommunityCommentMapper communityCommentMapper,
                                           UserMapper userMapper,
                                           StringRedisTemplate stringRedisTemplate,
                                           RedisKeyManager redisKeyManager) {
        this.communityBoardMapper = communityBoardMapper;
        this.communityPostMapper = communityPostMapper;
        this.communityPostFavoriteMapper = communityPostFavoriteMapper;
        this.communityCommentMapper = communityCommentMapper;
        this.userMapper = userMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisKeyManager = redisKeyManager;
    }

    /**
     * 发表新帖子，并清理首页和版区缓存。
     *
     * @param authorId 作者编号
     * @param postCreateDTO 发帖请求
     * @return 帖子详情
     */
    @Override
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
        communityPost.setPublishedAt(LocalDateTime.now());
        communityPostMapper.insert(communityPost);

        evictContentCaches();
        return communityPostMapper.selectDetailById(communityPost.getId());
    }

    /**
     * 查询当前用户可管理的帖子详情。
     *
     * @param authorId 作者编号
     * @param postId 帖子编号
     * @return 帖子详情
     */
    @Override
    public PostDetailVO getManagePost(Long authorId, Long postId) {
        CommunityPost communityPost = getOwnedPost(authorId, postId);
        PostDetailVO postDetailVO = communityPostMapper.selectDetailById(communityPost.getId());
        if (postDetailVO == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "目标帖子不存在");
        }
        return postDetailVO;
    }

    /**
     * 更新帖子内容并清理内容缓存。
     *
     * @param authorId 作者编号
     * @param postId 帖子编号
     * @param postUpdateDTO 更新请求
     * @return 更新后的帖子详情
     */
    @Override
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
        communityPostMapper.updatePost(existingPost);

        evictContentCaches();
        return communityPostMapper.selectDetailById(postId);
    }

    /**
     * 删除指定作者名下的帖子。
     *
     * @param authorId 作者编号
     * @param postId 帖子编号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long authorId, Long postId) {
        getOwnedPost(authorId, postId);
        int affectedRows = communityPostMapper.softDeleteByIdAndAuthorId(postId, authorId);
        if (affectedRows <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "帖子删除失败");
        }
        evictContentCaches();
    }

    /**
     * 发表评论，并同步增加帖子评论数。
     *
     * @param authorId 作者编号
     * @param postId 帖子编号
     * @param postCommentCreateDTO 评论请求
     * @return 评论详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostCommentVO createComment(Long authorId, Long postId, PostCommentCreateDTO postCommentCreateDTO) {
        validateCommentRequest(postCommentCreateDTO);
        User author = getExistingUser(authorId);
        getPublishedPost(postId);

        CommunityComment communityComment = new CommunityComment();
        communityComment.setPostId(postId);
        communityComment.setAuthorId(author.getId());
        communityComment.setContent(postCommentCreateDTO.getContent().trim());
        communityComment.setStatus(STATUS_ENABLED);
        communityCommentMapper.insert(communityComment);
        communityPostMapper.incrementCommentCount(postId);

        evictContentCaches();
        return communityCommentMapper.selectByPostId(postId).stream()
                .filter(comment -> comment.getId() != null && comment.getId().equals(communityComment.getId()))
                .findFirst()
                .orElseGet(() -> {
                    PostCommentVO postCommentVO = new PostCommentVO();
                    postCommentVO.setId(communityComment.getId());
                    postCommentVO.setPostId(postId);
                    postCommentVO.setAuthorId(author.getId());
                    postCommentVO.setAuthorName(StringUtils.hasText(author.getNickname()) ? author.getNickname() : author.getUsername());
                    postCommentVO.setAuthorAvatar(author.getAvatar());
                    postCommentVO.setContent(communityComment.getContent());
                    postCommentVO.setCreateTime(communityComment.getCreateTime());
                    return postCommentVO;
                });
    }

    /**
     * 查询当前用户对帖子的收藏状态。
     *
     * @param userId 用户编号
     * @param postId 帖子编号
     * @return 收藏状态
     */
    @Override
    public PostFavoriteStateVO getFavoriteState(Long userId, Long postId) {
        getExistingUser(userId);
        CommunityPost communityPost = getPublishedPost(postId);
        Integer favoriteStatus = communityPostFavoriteMapper.selectStatus(postId, userId);
        return buildFavoriteState(communityPost.getId(), favoriteStatus != null && favoriteStatus == STATUS_ENABLED, communityPost.getFavoriteCount());
    }

    /**
     * 收藏帖子。
     *
     * @param userId 用户编号
     * @param postId 帖子编号
     * @return 收藏状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostFavoriteStateVO favoritePost(Long userId, Long postId) {
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
        } else if (favoriteStatus == STATUS_DISABLED) {
            communityPostFavoriteMapper.activate(postId, userId);
            communityPostMapper.incrementFavoriteCount(postId);
        }

        evictContentCaches();
        CommunityPost refreshedPost = getPublishedPost(postId);
        return buildFavoriteState(postId, true, refreshedPost.getFavoriteCount());
    }

    /**
     * 取消收藏帖子。
     *
     * @param userId 用户编号
     * @param postId 帖子编号
     * @return 收藏状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostFavoriteStateVO unfavoritePost(Long userId, Long postId) {
        getExistingUser(userId);
        CommunityPost communityPost = getPublishedPost(postId);
        Integer favoriteStatus = communityPostFavoriteMapper.selectStatus(postId, userId);
        if (favoriteStatus != null && favoriteStatus == STATUS_ENABLED) {
            communityPostFavoriteMapper.deactivate(postId, userId);
            communityPostMapper.decrementFavoriteCount(postId);
            communityPost = getPublishedPost(postId);
        }

        evictContentCaches();
        return buildFavoriteState(postId, false, communityPost.getFavoriteCount());
    }

    /**
     * 校验帖子主体请求。
     *
     * @param boardId 所属版块编号
     * @param title 帖子标题
     * @param summary 帖子摘要
     * @param content 帖子正文
     */
    private void validatePostPayload(Long boardId, String title, String summary, String content) {
        if (boardId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请选择目标版块");
        }
        if (!StringUtils.hasText(title)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "帖子标题不能为空");
        }
        if (!StringUtils.hasText(summary)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "帖子摘要不能为空");
        }
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "帖子正文不能为空");
        }
        if (title.trim().length() > 120) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "帖子标题长度不能超过 120 个字符");
        }
        if (summary.trim().length() > 255) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "帖子摘要长度不能超过 255 个字符");
        }
    }

    /**
     * 校验评论请求。
     *
     * @param postCommentCreateDTO 评论请求
     */
    private void validateCommentRequest(PostCommentCreateDTO postCommentCreateDTO) {
        if (postCommentCreateDTO == null || !StringUtils.hasText(postCommentCreateDTO.getContent())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "评论内容不能为空");
        }
        if (postCommentCreateDTO.getContent().trim().length() > 1000) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "评论内容长度不能超过 1000 个字符");
        }
    }

    /**
     * 查询存在的用户。
     *
     * @param userId 用户编号
     * @return 用户实体
     */
    private User getExistingUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "当前用户不存在");
        }
        return user;
    }

    /**
     * 查询已发布帖子实体。
     *
     * @param postId 帖子编号
     * @return 帖子实体
     */
    private CommunityPost getPublishedPost(Long postId) {
        CommunityPost communityPost = communityPostMapper.selectById(postId);
        if (communityPost == null || communityPost.getStatus() == null || communityPost.getStatus() != STATUS_ENABLED) {
            throw new BusinessException(ResultCode.NOT_FOUND, "目标帖子不存在");
        }
        return communityPost;
    }

    /**
     * 查询属于指定作者的可编辑帖子。
     *
     * @param authorId 作者编号
     * @param postId 帖子编号
     * @return 帖子实体
     */
    private CommunityPost getOwnedPost(Long authorId, Long postId) {
        CommunityPost communityPost = getPublishedPost(postId);
        if (communityPost.getAuthorId() == null || !communityPost.getAuthorId().equals(authorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能管理自己发布的帖子");
        }
        return communityPost;
    }

    /**
     * 查询存在的版块。
     *
     * @param boardId 版块编号
     * @return 版块实体
     */
    private CommunityBoard getExistingBoard(Long boardId) {
        CommunityBoard communityBoard = communityBoardMapper.selectById(boardId);
        if (communityBoard == null || communityBoard.getStatus() == null || communityBoard.getStatus() != STATUS_ENABLED) {
            throw new BusinessException(ResultCode.NOT_FOUND, "目标版块不存在");
        }
        return communityBoard;
    }

    /**
     * 规范化可空文本。
     *
     * @param value 原始文本
     * @return 规范化结果
     */
    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 构造帖子收藏状态视图。
     *
     * @param postId 帖子编号
     * @param favorited 是否已收藏
     * @param favoriteCount 收藏数
     * @return 收藏状态
     */
    private PostFavoriteStateVO buildFavoriteState(Long postId, boolean favorited, Integer favoriteCount) {
        PostFavoriteStateVO postFavoriteStateVO = new PostFavoriteStateVO();
        postFavoriteStateVO.setPostId(postId);
        postFavoriteStateVO.setFavorited(favorited);
        postFavoriteStateVO.setFavoriteCount(favoriteCount == null ? 0 : favoriteCount);
        return postFavoriteStateVO;
    }

    /**
     * 清理首页和版区缓存，确保发帖/评论后页面能读到最新数据。
     */
    private void evictContentCaches() {
        try {
            stringRedisTemplate.delete(redisKeyManager.buildKey(CommunityContentCacheKeys.HOME_PAGE));
            stringRedisTemplate.delete(redisKeyManager.buildKey(CommunityContentCacheKeys.BOARDS_PAGE));
        } catch (Exception exception) {
            log.warn("Community cache eviction skipped", exception);
        }
    }
}
