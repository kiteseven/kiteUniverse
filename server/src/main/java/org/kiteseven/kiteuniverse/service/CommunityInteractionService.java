package org.kiteseven.kiteuniverse.service;

import org.kiteseven.kiteuniverse.pojo.dto.community.PostCommentCreateDTO;
import org.kiteseven.kiteuniverse.pojo.dto.community.PostCreateDTO;
import org.kiteseven.kiteuniverse.pojo.dto.community.PostUpdateDTO;
import org.kiteseven.kiteuniverse.pojo.vo.community.CommentLikeStateVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostCommentVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostDetailVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostFavoriteStateVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostLikeStateVO;

/**
 * 提供社区发帖和评论等互动能力。
 */
public interface CommunityInteractionService {

    /**
     * 发表新帖子。
     *
     * @param authorId 作者编号
     * @param postCreateDTO 发帖请求
     * @return 新创建的帖子详情
     */
    PostDetailVO createPost(Long authorId, PostCreateDTO postCreateDTO);

    /**
     * 查询当前用户可管理的帖子详情。
     *
     * @param authorId 作者编号
     * @param postId 帖子编号
     * @return 帖子详情
     */
    PostDetailVO getManagePost(Long authorId, Long postId);

    /**
     * 更新帖子内容。
     *
     * @param authorId 作者编号
     * @param postId 帖子编号
     * @param postUpdateDTO 更新请求
     * @return 更新后的帖子详情
     */
    PostDetailVO updatePost(Long authorId, Long postId, PostUpdateDTO postUpdateDTO);

    /**
     * 删除帖子。
     *
     * @param authorId 作者编号
     * @param postId 帖子编号
     */
    void deletePost(Long authorId, Long postId);

    /**
     * 发表评论。
     *
     * @param authorId 作者编号
     * @param postId 帖子编号
     * @param postCommentCreateDTO 评论请求
     * @return 新创建的评论
     */
    PostCommentVO createComment(Long authorId, Long postId, PostCommentCreateDTO postCommentCreateDTO);

    /**
     * 查询当前用户对帖子的收藏状态。
     *
     * @param userId 用户编号
     * @param postId 帖子编号
     * @return 收藏状态
     */
    PostFavoriteStateVO getFavoriteState(Long userId, Long postId);

    /**
     * 收藏帖子。
     *
     * @param userId 用户编号
     * @param postId 帖子编号
     * @return 收藏状态
     */
    PostFavoriteStateVO favoritePost(Long userId, Long postId);

    /**
     * 取消收藏帖子。
     *
     * @param userId 用户编号
     * @param postId 帖子编号
     * @return 收藏状态
     */
    PostFavoriteStateVO unfavoritePost(Long userId, Long postId);

    /**
     * 查询当前用户对帖子的点赞状态。
     *
     * @param userId 用户编号
     * @param postId 帖子编号
     * @return 点赞状态
     */
    PostLikeStateVO getLikeState(Long userId, Long postId);

    /**
     * 点赞帖子。
     *
     * @param userId 用户编号
     * @param postId 帖子编号
     * @return 点赞状态
     */
    PostLikeStateVO likePost(Long userId, Long postId);

    /**
     * 取消点赞帖子。
     *
     * @param userId 用户编号
     * @param postId 帖子编号
     * @return 点赞状态
     */
    PostLikeStateVO unlikePost(Long userId, Long postId);

    /**
     * 查询当前用户对评论的点赞状态。
     *
     * @param userId 用户编号
     * @param commentId 评论编号
     * @return 点赞状态
     */
    CommentLikeStateVO getCommentLikeState(Long userId, Long commentId);

    /**
     * 点赞评论。
     *
     * @param userId 用户编号
     * @param commentId 评论编号
     * @return 点赞状态
     */
    CommentLikeStateVO likeComment(Long userId, Long commentId);

    /**
     * 取消点赞评论。
     *
     * @param userId 用户编号
     * @param commentId 评论编号
     * @return 点赞状态
     */
    CommentLikeStateVO unlikeComment(Long userId, Long commentId);
}
