package org.kiteseven.kiteuniverse.pojo.vo.community;

import java.time.LocalDateTime;

/**
 * 帖子详情视图对象。
 */
public class PostDetailVO {

    /**
     * 帖子编号。
     */
    private Long id;

    /**
     * 所属版块编号。
     */
    private Long boardId;

    /**
     * 所属版块名称。
     */
    private String boardName;

    /**
     * 所属版块别名。
     */
    private String boardSlug;

    /**
     * 所属版块标签。
     */
    private String boardTagName;

    /**
     * 作者编号。
     */
    private Long authorId;

    /**
     * 作者名称。
     */
    private String authorName;

    /**
     * 作者头像。
     */
    private String authorAvatar;

    /**
     * 帖子徽标。
     */
    private String badge;

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
     * 发布时间。
     */
    private LocalDateTime publishedAt;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }

    public String getBoardSlug() {
        return boardSlug;
    }

    public void setBoardSlug(String boardSlug) {
        this.boardSlug = boardSlug;
    }

    public String getBoardTagName() {
        return boardTagName;
    }

    public void setBoardTagName(String boardTagName) {
        this.boardTagName = boardTagName;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
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

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
