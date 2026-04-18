package org.kiteseven.kiteuniverse.pojo.vo.admin;

/**
 * 管理后台数据统计面板 VO。
 */
public class AdminStatsVO {

    private long totalUsers;
    private long newUsersToday;
    private long activeUsersToday;
    private long totalPosts;
    private long newPostsToday;
    private long totalComments;
    private long pendingReports;
    private long bannedUsers;

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getNewUsersToday() { return newUsersToday; }
    public void setNewUsersToday(long newUsersToday) { this.newUsersToday = newUsersToday; }

    public long getActiveUsersToday() { return activeUsersToday; }
    public void setActiveUsersToday(long activeUsersToday) { this.activeUsersToday = activeUsersToday; }

    public long getTotalPosts() { return totalPosts; }
    public void setTotalPosts(long totalPosts) { this.totalPosts = totalPosts; }

    public long getNewPostsToday() { return newPostsToday; }
    public void setNewPostsToday(long newPostsToday) { this.newPostsToday = newPostsToday; }

    public long getTotalComments() { return totalComments; }
    public void setTotalComments(long totalComments) { this.totalComments = totalComments; }

    public long getPendingReports() { return pendingReports; }
    public void setPendingReports(long pendingReports) { this.pendingReports = pendingReports; }

    public long getBannedUsers() { return bannedUsers; }
    public void setBannedUsers(long bannedUsers) { this.bannedUsers = bannedUsers; }
}
