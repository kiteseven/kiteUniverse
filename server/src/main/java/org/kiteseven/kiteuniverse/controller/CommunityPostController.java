package org.kiteseven.kiteuniverse.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.kiteseven.kiteuniverse.common.result.Result;
import org.kiteseven.kiteuniverse.pojo.dto.community.PostCommentCreateDTO;
import org.kiteseven.kiteuniverse.pojo.dto.community.PostCreateDTO;
import org.kiteseven.kiteuniverse.pojo.dto.community.PostUpdateDTO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostCommentVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostDetailVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostFavoriteStateVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostSummaryVO;
import org.kiteseven.kiteuniverse.service.CommunityInteractionService;
import org.kiteseven.kiteuniverse.service.CommunityQueryService;
import org.kiteseven.kiteuniverse.support.auth.UserTokenService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 提供社区帖子详情、发帖与评论接口。
 */
@RestController
@RequestMapping("/api/posts")
public class CommunityPostController {

    private final CommunityQueryService communityQueryService;
    private final CommunityInteractionService communityInteractionService;
    private final UserTokenService userTokenService;

    public CommunityPostController(CommunityQueryService communityQueryService,
                                   CommunityInteractionService communityInteractionService,
                                   UserTokenService userTokenService) {
        this.communityQueryService = communityQueryService;
        this.communityInteractionService = communityInteractionService;
        this.userTokenService = userTokenService;
    }

    /**
     * 按关键字搜索帖子。
     *
     * @param keyword 搜索关键字
     * @param limit   查询条数
     * @return 帖子概要列表
     */
    @GetMapping("/search")
    public Result<List<PostSummaryVO>> searchPosts(@RequestParam String keyword,
                                                   @RequestParam(defaultValue = "20") int limit) {
        return Result.success(communityQueryService.searchPosts(keyword, limit));
    }

    /**
     * 查询首页精选帖子。
     *
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    @GetMapping("/featured")
    public Result<List<PostSummaryVO>> listFeaturedPosts(@RequestParam(defaultValue = "3") int limit) {
        return Result.success(communityQueryService.listFeaturedPosts(limit));
    }

    /**
     * 查询最新帖子。
     *
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    @GetMapping("/latest")
    public Result<List<PostSummaryVO>> listLatestPosts(@RequestParam(defaultValue = "3") int limit) {
        return Result.success(communityQueryService.listLatestPosts(limit));
    }

    /**
     * 查询指定版块下的帖子列表。
     *
     * @param boardId 版块编号
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    @GetMapping("/board/{boardId}")
    public Result<List<PostSummaryVO>> listPostsByBoard(@PathVariable Long boardId,
                                                        @RequestParam(defaultValue = "10") int limit) {
        return Result.success(communityQueryService.listPostsByBoardId(boardId, limit));
    }

    /**
     * 查询当前用户发布的帖子列表。
     *
     * @param request 当前请求
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    @GetMapping("/mine")
    public Result<List<PostSummaryVO>> listMyPosts(HttpServletRequest request,
                                                   @RequestParam(defaultValue = "10") int limit) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityQueryService.listPostsByAuthorId(userId, limit));
    }

    /**
     * 查询当前用户收藏的帖子列表。
     *
     * @param request 当前请求
     * @param limit 查询条数
     * @return 帖子概要列表
     */
    @GetMapping("/mine/favorites")
    public Result<List<PostSummaryVO>> listMyFavoritePosts(HttpServletRequest request,
                                                           @RequestParam(defaultValue = "10") int limit) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityQueryService.listFavoritePostsByUserId(userId, limit));
    }

    /**
     * 查询帖子详情。
     *
     * @param postId 帖子编号
     * @return 帖子详情
     */
    @GetMapping("/{postId}")
    public Result<PostDetailVO> getPostDetail(@PathVariable Long postId) {
        return Result.success(communityQueryService.getPostDetail(postId));
    }

    /**
     * 查询当前用户可管理的帖子详情。
     *
     * @param request 当前请求
     * @param postId 帖子编号
     * @return 帖子详情
     */
    @GetMapping("/{postId}/manage")
    public Result<PostDetailVO> getManagePost(HttpServletRequest request, @PathVariable Long postId) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.getManagePost(userId, postId));
    }

    /**
     * 查询帖子评论列表。
     *
     * @param postId 帖子编号
     * @return 评论列表
     */
    @GetMapping("/{postId}/comments")
    public Result<List<PostCommentVO>> listComments(@PathVariable Long postId) {
        return Result.success(communityQueryService.listCommentsByPostId(postId));
    }

    /**
     * 查询当前用户对指定帖子的收藏状态。
     *
     * @param request 当前请求
     * @param postId 帖子编号
     * @return 收藏状态
     */
    @GetMapping("/{postId}/favorite-state")
    public Result<PostFavoriteStateVO> getFavoriteState(HttpServletRequest request, @PathVariable Long postId) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.getFavoriteState(userId, postId));
    }

    /**
     * 发表新帖子。
     *
     * @param request 当前请求
     * @param postCreateDTO 发帖请求
     * @return 新创建的帖子详情
     */
    @PostMapping
    public Result<PostDetailVO> createPost(HttpServletRequest request, @RequestBody PostCreateDTO postCreateDTO) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.createPost(userId, postCreateDTO));
    }

    /**
     * 更新帖子内容。
     *
     * @param request 当前请求
     * @param postId 帖子编号
     * @param postUpdateDTO 更新请求
     * @return 更新后的帖子详情
     */
    @PutMapping("/{postId}")
    public Result<PostDetailVO> updatePost(HttpServletRequest request,
                                           @PathVariable Long postId,
                                           @RequestBody PostUpdateDTO postUpdateDTO) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.updatePost(userId, postId, postUpdateDTO));
    }

    /**
     * 删除帖子。
     *
     * @param request 当前请求
     * @param postId 帖子编号
     * @return 空响应
     */
    @DeleteMapping("/{postId}")
    public Result<Void> deletePost(HttpServletRequest request, @PathVariable Long postId) {
        Long userId = resolveCurrentUserId(request);
        communityInteractionService.deletePost(userId, postId);
        return Result.success();
    }

    /**
     * 发表帖子评论。
     *
     * @param request 当前请求
     * @param postId 帖子编号
     * @param postCommentCreateDTO 评论请求
     * @return 新创建的评论
     */
    @PostMapping("/{postId}/comments")
    public Result<PostCommentVO> createComment(HttpServletRequest request,
                                               @PathVariable Long postId,
                                               @RequestBody PostCommentCreateDTO postCommentCreateDTO) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.createComment(userId, postId, postCommentCreateDTO));
    }

    /**
     * 收藏帖子。
     *
     * @param request 当前请求
     * @param postId 帖子编号
     * @return 收藏状态
     */
    @PostMapping("/{postId}/favorite")
    public Result<PostFavoriteStateVO> favoritePost(HttpServletRequest request, @PathVariable Long postId) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.favoritePost(userId, postId));
    }

    /**
     * 取消收藏帖子。
     *
     * @param request 当前请求
     * @param postId 帖子编号
     * @return 收藏状态
     */
    @DeleteMapping("/{postId}/favorite")
    public Result<PostFavoriteStateVO> unfavoritePost(HttpServletRequest request, @PathVariable Long postId) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.unfavoritePost(userId, postId));
    }

    /**
     * 从请求令牌中解析当前用户编号。
     *
     * @param request 当前请求
     * @return 用户编号
     */
    private Long resolveCurrentUserId(HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        return userTokenService.parseUserId(token);
    }
}
