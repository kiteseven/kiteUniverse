package org.kiteseven.kiteuniverse.pojo.vo.community;

/**
 * 帖子收藏状态视图对象。
 */
public class PostFavoriteStateVO {

    /**
     * 帖子编号。
     */
    private Long postId;

    /**
     * 当前用户是否已收藏。
     */
    private Boolean favorited;

    /**
     * 当前帖子收藏数。
     */
    private Integer favoriteCount;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Boolean getFavorited() {
        return favorited;
    }

    public void setFavorited(Boolean favorited) {
        this.favorited = favorited;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }
}
