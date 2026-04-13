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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
