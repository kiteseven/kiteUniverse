package org.kiteseven.kiteuniverse.pojo.vo.community;

import java.time.LocalDateTime;

/**
 * 帖子评论视图对象。
 */
public class PostCommentVO {

    /**
     * 评论编号。
     */
    private Long id;

    /**
     * 帖子编号。
     */
    private Long postId;

    /**
     * 评论用户编号。
     */
    private Long authorId;

    /**
     * 评论用户名称。
     */
    private String authorName;

    /**
     * 评论用户头像。
     */
    private String authorAvatar;

    /**
     * 评论内容。
     */
    private String content;

    /**
     * 点赞数。
     */
    private Integer likeCount;

    /**
     * 当前用户是否已点赞。
     */
    private Boolean liked;

    /**
     * 父评论编号（null 表示顶层评论）。
     */
    private Long parentId;

    /**
     * 被回复用户名称（二级回复时展示）。
     */
    private String replyToName;

    /**
     * 评论创建时间。
     */
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getReplyToName() {
        return replyToName;
    }

    public void setReplyToName(String replyToName) {
        this.replyToName = replyToName;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
