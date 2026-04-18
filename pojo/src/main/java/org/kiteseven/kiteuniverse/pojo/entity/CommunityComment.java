package org.kiteseven.kiteuniverse.pojo.entity;

import org.kiteseven.kiteuniverse.pojo.base.BaseEntity;

import java.io.Serial;

/**
 * 社区评论实体类。
 */
public class CommunityComment extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属帖子编号。
     */
    private Long postId;

    /**
     * 父评论编号（null 表示顶层评论）。
     */
    private Long parentId;

    /**
     * 评论用户编号。
     */
    private Long authorId;

    /**
     * 评论内容。
     */
    private String content;

    /**
     * 评论状态，1 表示正常。
     */
    private Integer status;

    /**
     * 点赞数。
     */
    private Integer likeCount;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
}
