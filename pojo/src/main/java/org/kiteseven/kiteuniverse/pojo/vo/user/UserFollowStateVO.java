package org.kiteseven.kiteuniverse.pojo.vo.user;

/**
 * 用户关注状态视图对象。
 */
public class UserFollowStateVO {

    /**
     * 目标用户编号。
     */
    private Long userId;

    /**
     * 当前用户是否已关注。
     */
    private Boolean followed;

    /**
     * 粉丝数。
     */
    private Integer followerCount;

    /**
     * 关注数。
     */
    private Integer followingCount;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getFollowed() {
        return followed;
    }

    public void setFollowed(Boolean followed) {
        this.followed = followed;
    }

    public Integer getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(Integer followerCount) {
        this.followerCount = followerCount;
    }

    public Integer getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Integer followingCount) {
        this.followingCount = followingCount;
    }
}
