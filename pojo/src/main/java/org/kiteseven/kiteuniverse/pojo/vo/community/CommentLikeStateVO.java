package org.kiteseven.kiteuniverse.pojo.vo.community;

/**
 * 评论点赞状态视图对象。
 */
public class CommentLikeStateVO {

    /**
     * 评论编号。
     */
    private Long commentId;

    /**
     * 当前用户是否已点赞。
     */
    private Boolean liked;

    /**
     * 当前评论点赞数。
     */
    private Integer likeCount;

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
}
