package org.kiteseven.kiteuniverse.pojo.vo.admin;

import java.time.LocalDateTime;

/**
 * 管理后台帖子审核列表行 VO。
 */
public class AdminPostVO {

    private Long id;
    private String title;
    private String authorName;
    private Long authorId;
    private String boardName;
    private Integer status;
    private Boolean pinned;
    private Boolean featured;
    private Integer viewCount;
    private Integer commentCount;
    private Integer likeCount;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getBoardName() { return boardName; }
    public void setBoardName(String boardName) { this.boardName = boardName; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Boolean getPinned() { return pinned; }
    public void setPinned(Boolean pinned) { this.pinned = pinned; }

    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
