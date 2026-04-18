package org.kiteseven.kiteuniverse.pojo.entity;

import org.kiteseven.kiteuniverse.pojo.base.BaseEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 社区帖子实体类。
 */
public class CommunityPost extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属版块编号。
     */
    private Long boardId;

    /**
     * 发帖用户编号。
     */
    private Long authorId;

    /**
     * 帖子标题。
     */
    private String title;

    /**
     * 帖子摘要。
     */
    private String summary;

    /**
     * 帖子正文。
     */
    private String content;

    /**
     * 帖子徽标文案。
     */
    private String badge;

    /**
     * 帖子状态，1 表示已发布。
     */
    private Integer status;

    /**
     * 是否首页精选，1 表示是。
     */
    private Integer featured;

    /**
     * 是否置顶，1 表示是。
     */
    private Integer pinned;

    /**
     * 浏览量。
     */
    private Integer viewCount;

    /**
     * 评论数。
     */
    private Integer commentCount;

    /**
     * 收藏数。
     */
    private Integer favoriteCount;

    /**
     * 点赞数。
     */
    private Integer likeCount;

    /**
     * 是否包含 AI 生成内容，1 表示是。
     */
    private Integer isAiGenerated;

    /**
     * 图集图片 URL 列表，JSON 数组字符串，空帖为 null。
     */
    private String galleryImages;

    /**
     * 发布时间。
     */
    private LocalDateTime publishedAt;

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getFeatured() {
        return featured;
    }

    public void setFeatured(Integer featured) {
        this.featured = featured;
    }

    public Integer getPinned() {
        return pinned;
    }

    public void setPinned(Integer pinned) {
        this.pinned = pinned;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getIsAiGenerated() {
        return isAiGenerated;
    }

    public void setIsAiGenerated(Integer isAiGenerated) {
        this.isAiGenerated = isAiGenerated;
    }

    public String getGalleryImages() {
        return galleryImages;
    }

    public void setGalleryImages(String galleryImages) {
        this.galleryImages = galleryImages;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}
