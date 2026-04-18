package org.kiteseven.kiteuniverse.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.kiteseven.kiteuniverse.common.result.Result;
import org.kiteseven.kiteuniverse.pojo.dto.community.PostCommentCreateDTO;
import org.kiteseven.kiteuniverse.pojo.dto.community.PostCreateDTO;
import org.kiteseven.kiteuniverse.pojo.dto.community.PostUpdateDTO;
import org.kiteseven.kiteuniverse.pojo.vo.community.CommentLikeStateVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostCommentVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostDetailVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostFavoriteStateVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostLikeStateVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostSummaryVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.SearchStatsVO;
import org.kiteseven.kiteuniverse.service.CommunityInteractionService;
import org.kiteseven.kiteuniverse.service.CommunityQueryService;
import org.kiteseven.kiteuniverse.service.PostIndexService;
import org.kiteseven.kiteuniverse.support.auth.UserTokenService;
import org.kiteseven.kiteuniverse.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 提供社区帖子详情、发帖与评论接口。
 */
@RestController
@RequestMapping("/api/posts")
public class CommunityPostController {

    private final CommunityQueryService communityQueryService;
    private final CommunityInteractionService communityInteractionService;
    private final UserTokenService userTokenService;
    private final FileStorageService fileStorageService;
    private final PostIndexService postIndexService;

    public CommunityPostController(CommunityQueryService communityQueryService,
                                   CommunityInteractionService communityInteractionService,
                                   UserTokenService userTokenService,
                                   FileStorageService fileStorageService,
                                   PostIndexService postIndexService) {
        this.communityQueryService = communityQueryService;
        this.communityInteractionService = communityInteractionService;
        this.userTokenService = userTokenService;
        this.fileStorageService = fileStorageService;
        this.postIndexService = postIndexService;
    }

    /**
     * 按关键字搜索帖子。
     */
    @GetMapping("/search")
    public Result<List<PostSummaryVO>> searchPosts(@RequestParam String keyword,
                                                   @RequestParam(defaultValue = "20") int limit) {
        return Result.success(communityQueryService.searchPosts(keyword, limit));
    }

    /**
     * 查询首页精选帖子。
     */
    @GetMapping("/featured")
    public Result<List<PostSummaryVO>> listFeaturedPosts(@RequestParam(defaultValue = "3") int limit) {
        return Result.success(communityQueryService.listFeaturedPosts(limit));
    }

    /**
     * 查询最新帖子。
     */
    @GetMapping("/latest")
    public Result<List<PostSummaryVO>> listLatestPosts(@RequestParam(defaultValue = "3") int limit) {
        return Result.success(communityQueryService.listLatestPosts(limit));
    }

    /**
     * 查询指定版块下的帖子列表。
     */
    @GetMapping("/board/{boardId}")
    public Result<List<PostSummaryVO>> listPostsByBoard(@PathVariable Long boardId,
                                                        @RequestParam(defaultValue = "10") int limit) {
        return Result.success(communityQueryService.listPostsByBoardId(boardId, limit));
    }

    /**
     * 查询指定版块帖子（支持排序和分页）。
     */
    @GetMapping("/board/{boardId}/paged")
    public Result<List<PostSummaryVO>> listPostsByBoardPaged(@PathVariable Long boardId,
                                                              @RequestParam(defaultValue = "20") int limit,
                                                              @RequestParam(defaultValue = "0") int offset,
                                                              @RequestParam(defaultValue = "latest") String sort) {
        return Result.success(communityQueryService.listPostsByBoardIdPaged(boardId, limit, offset, sort));
    }

    /**
     * 统计指定版块帖子总数。
     */
    @GetMapping("/board/{boardId}/count")
    public Result<Long> countPostsByBoard(@PathVariable Long boardId,
                                          @RequestParam(defaultValue = "latest") String sort) {
        return Result.success(communityQueryService.countPostsByBoardId(boardId, sort));
    }

    /**
     * 查询热门帖子。
     */
    @GetMapping("/hot")
    public Result<List<PostSummaryVO>> listHotPosts(@RequestParam(defaultValue = "10") int limit,
                                                     @RequestParam(defaultValue = "7") int days) {
        return Result.success(communityQueryService.listHotPosts(limit, days));
    }

    /**
     * 按话题徽标聚合查询帖子。
     */
    @GetMapping("/badge/{badge}")
    public Result<List<PostSummaryVO>> listPostsByBadge(@PathVariable String badge,
                                                         @RequestParam(defaultValue = "20") int limit,
                                                         @RequestParam(defaultValue = "0") int offset) {
        return Result.success(communityQueryService.listPostsByBadge(badge, limit, offset));
    }

    /**
     * 查询个性化推荐帖子（需要登录）。
     */
    @GetMapping("/recommended")
    public Result<List<PostSummaryVO>> listRecommendedPosts(HttpServletRequest request,
                                                             @RequestParam(defaultValue = "10") int limit) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityQueryService.listRecommendedPosts(userId, limit));
    }

    /**
     * 查询当前用户发布的帖子列表。
     */
    @GetMapping("/mine")
    public Result<List<PostSummaryVO>> listMyPosts(HttpServletRequest request,
                                                   @RequestParam(defaultValue = "10") int limit) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityQueryService.listPostsByAuthorId(userId, limit));
    }

    /**
     * 查询当前用户收藏的帖子列表。
     */
    @GetMapping("/mine/favorites")
    public Result<List<PostSummaryVO>> listMyFavoritePosts(HttpServletRequest request,
                                                           @RequestParam(defaultValue = "10") int limit) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityQueryService.listFavoritePostsByUserId(userId, limit));
    }

    /**
     * 查询帖子详情。
     */
    @GetMapping("/{postId}")
    public Result<PostDetailVO> getPostDetail(@PathVariable Long postId) {
        return Result.success(communityQueryService.getPostDetail(postId));
    }

    /**
     * 查询当前用户可管理的帖子详情。
     */
    @GetMapping("/{postId}/manage")
    public Result<PostDetailVO> getManagePost(HttpServletRequest request, @PathVariable Long postId) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.getManagePost(userId, postId));
    }

    /**
     * 查询帖子评论列表（可选携带当前用户的点赞状态）。
     */
    @GetMapping("/{postId}/comments")
    public Result<List<PostCommentVO>> listComments(HttpServletRequest request, @PathVariable Long postId) {
        Long userId = resolveOptionalUserId(request);
        return Result.success(communityQueryService.listCommentsByPostId(postId, userId));
    }

    /**
     * 查询当前用户对指定帖子的收藏状态。
     */
    @GetMapping("/{postId}/favorite-state")
    public Result<PostFavoriteStateVO> getFavoriteState(HttpServletRequest request, @PathVariable Long postId) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.getFavoriteState(userId, postId));
    }

    /**
     * 发表新帖子。
     */
    @PostMapping
    public Result<PostDetailVO> createPost(HttpServletRequest request, @RequestBody PostCreateDTO postCreateDTO) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.createPost(userId, postCreateDTO));
    }

    /**
     * 更新帖子内容。
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
     */
    @DeleteMapping("/{postId}")
    public Result<Void> deletePost(HttpServletRequest request, @PathVariable Long postId) {
        Long userId = resolveCurrentUserId(request);
        communityInteractionService.deletePost(userId, postId);
        return Result.success();
    }

    /**
     * 发表帖子评论。
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
     */
    @PostMapping("/{postId}/favorite")
    public Result<PostFavoriteStateVO> favoritePost(HttpServletRequest request, @PathVariable Long postId) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.favoritePost(userId, postId));
    }

    /**
     * 取消收藏帖子。
     */
    @DeleteMapping("/{postId}/favorite")
    public Result<PostFavoriteStateVO> unfavoritePost(HttpServletRequest request, @PathVariable Long postId) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.unfavoritePost(userId, postId));
    }

    /**
     * 查询指定用户发布的帖子列表。
     */
    @GetMapping("/user/{userId}")
    public Result<List<PostSummaryVO>> listUserPosts(@PathVariable Long userId,
                                                      @RequestParam(defaultValue = "10") int limit) {
        return Result.success(communityQueryService.listPostsByAuthorId(userId, limit));
    }

    /**
     * 查询当前用户对指定帖子的点赞状态。
     */
    @GetMapping("/{postId}/like-state")
    public Result<PostLikeStateVO> getLikeState(HttpServletRequest request, @PathVariable Long postId) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.getLikeState(userId, postId));
    }

    /**
     * 点赞帖子。
     */
    @PostMapping("/{postId}/like")
    public Result<PostLikeStateVO> likePost(HttpServletRequest request, @PathVariable Long postId) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.likePost(userId, postId));
    }

    /**
     * 取消点赞帖子。
     */
    @DeleteMapping("/{postId}/like")
    public Result<PostLikeStateVO> unlikePost(HttpServletRequest request, @PathVariable Long postId) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.unlikePost(userId, postId));
    }

    /**
     * 查询当前用户对指定评论的点赞状态。
     */
    @GetMapping("/{postId}/comments/{commentId}/like-state")
    public Result<CommentLikeStateVO> getCommentLikeState(HttpServletRequest request,
                                                           @PathVariable Long postId,
                                                           @PathVariable Long commentId) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.getCommentLikeState(userId, commentId));
    }

    /**
     * 点赞评论。
     */
    @PostMapping("/{postId}/comments/{commentId}/like")
    public Result<CommentLikeStateVO> likeComment(HttpServletRequest request,
                                                   @PathVariable Long postId,
                                                   @PathVariable Long commentId) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.likeComment(userId, commentId));
    }

    /**
     * 取消点赞评论。
     */
    @DeleteMapping("/{postId}/comments/{commentId}/like")
    public Result<CommentLikeStateVO> unlikeComment(HttpServletRequest request,
                                                     @PathVariable Long postId,
                                                     @PathVariable Long commentId) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(communityInteractionService.unlikeComment(userId, commentId));
    }

    /**
     * 上传帖子图片，返回可访问的图片 URL。
     */
    @PostMapping(value = "/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> uploadPostImage(HttpServletRequest request,
                                                       @RequestParam("file") MultipartFile file) {
        Long userId = resolveCurrentUserId(request);
        String url = fileStorageService.storePostImage(file, userId);
        return Result.success(Map.of("url", url));
    }

    /**
     * 搜索建议（自动补全）：按标题前缀返回候选帖子标题列表。
     */
    @GetMapping("/suggest")
    public Result<List<String>> suggest(@RequestParam String prefix,
                                        @RequestParam(defaultValue = "8") int limit) {
        if (prefix == null || prefix.isBlank()) {
            return Result.success(List.of());
        }
        try {
            return Result.success(postIndexService.suggest(prefix.trim(), limit));
        } catch (Exception e) {
            return Result.success(List.of());
        }
    }

    /**
     * 基于 more_like_this 推荐与指定帖子相关的帖子。
     */
    @GetMapping("/{postId}/related")
    public Result<List<PostSummaryVO>> relatedPosts(@PathVariable Long postId,
                                                     @RequestParam(defaultValue = "5") int limit) {
        try {
            return Result.success(postIndexService.findRelated(postId, limit));
        } catch (Exception e) {
            return Result.success(List.of());
        }
    }

    /**
     * 获取搜索统计看板数据（热门词 + 无结果率）。
     */
    @GetMapping("/search-stats")
    public Result<SearchStatsVO> searchStats(@RequestParam(defaultValue = "10") int topN) {
        return Result.success(postIndexService.getSearchStats(topN));
    }

    /**
     * 从请求令牌中解析当前用户编号（必须已登录）。
     */
    private Long resolveCurrentUserId(HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        return userTokenService.parseUserId(token);
    }

    /**
     * 从请求令牌中尝试解析用户编号（未登录时返回 null）。
     */
    private Long resolveOptionalUserId(HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) {
            return null;
        }
        try {
            return userTokenService.parseUserId(token);
        } catch (Exception e) {
            return null;
        }
    }
}
