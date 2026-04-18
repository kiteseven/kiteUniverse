package org.kiteseven.kiteuniverse.pojo.vo.community;

/**
 * 帖子点赞状态视图对象。
 */
public class PostLikeStateVO {

    /**
     * 帖子编号。
     */
    private Long postId;

    /**
     * 当前用户是否已点赞。
     */
    private Boolean liked;

    /**
     * 当前帖子点赞数。
     */
    private Integer likeCount;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
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
