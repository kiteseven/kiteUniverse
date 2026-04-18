package org.kiteseven.kiteuniverse.pojo.dto.community;

import java.time.LocalDateTime;

/**
 * 帖子索引数据传输对象，供 Elasticsearch 全文索引使用。
 * 包含正文在内的完整字段，不用于前端展示。
 */
public class PostIndexDTO {

    private Long id;
    private Long boardId;
    private String boardName;
    private String boardSlug;
    private String boardTagName;
    private Long authorId;
    private String authorName;
    private String title;
    private String summary;
    private String content;
    private String badge;
    private Integer status;
    private Integer viewCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Integer likeCount;
    private Boolean pinned;
    private Boolean featured;
    private Boolean aiGenerated;
    private LocalDateTime publishedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }

    public String getBoardName() { return boardName; }
    public void setBoardName(String boardName) { this.boardName = boardName; }

    public String getBoardSlug() { return boardSlug; }
    public void setBoardSlug(String boardSlug) { this.boardSlug = boardSlug; }

    public String getBoardTagName() { return boardTagName; }
    public void setBoardTagName(String boardTagName) { this.boardTagName = boardTagName; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }

    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Boolean getPinned() { return pinned; }
    public void setPinned(Boolean pinned) { this.pinned = pinned; }

    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }

    public Boolean getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(Boolean aiGenerated) { this.aiGenerated = aiGenerated; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
