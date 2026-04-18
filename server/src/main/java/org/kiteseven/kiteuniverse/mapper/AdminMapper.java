package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.vo.admin.AdminCommentVO;
import org.kiteseven.kiteuniverse.pojo.vo.admin.AdminPostVO;
import org.kiteseven.kiteuniverse.pojo.vo.admin.AdminUserVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台数据访问接口。
 */
@Mapper
public interface AdminMapper {

    /** 查询帖子总数。 */
    long countAllPosts();

    /** 查询今日新增帖子数。 */
    long countPostsCreatedSince(@Param("since") LocalDateTime since);

    /** 查询评论总数。 */
    long countAllComments();

    /** 查询被封禁用户数。 */
    long countBannedUsers();

    /** 分页查询用户列表。 */
    List<AdminUserVO> selectUsers(@Param("keyword") String keyword,
                                  @Param("status") Integer status,
                                  @Param("limit") int limit,
                                  @Param("offset") int offset);

    /** 查询用户总数（用于分页）。 */
    long countUsers(@Param("keyword") String keyword, @Param("status") Integer status);

    /** 更新用户状态（封禁/解封）。 */
    int updateUserStatus(@Param("id") Long id, @Param("status") int status);

    /** 更新用户禁言截止时间。 */
    int updateUserMuteUntil(@Param("id") Long id, @Param("muteUntil") LocalDateTime muteUntil);

    /** 更新用户角色。 */
    int updateUserRole(@Param("id") Long id, @Param("role") String role);

    /** 分页查询帖子列表（管理视角）。 */
    List<AdminPostVO> selectPosts(@Param("keyword") String keyword,
                                  @Param("status") Integer status,
                                  @Param("limit") int limit,
                                  @Param("offset") int offset);

    /** 查询帖子总数（管理视角）。 */
    long countPosts(@Param("keyword") String keyword, @Param("status") Integer status);

    /** 更新帖子状态（隐藏/恢复）。 */
    int updatePostStatus(@Param("id") Long id, @Param("status") int status);

    /** 更新帖子置顶状态。 */
    int updatePostPinned(@Param("id") Long id, @Param("pinned") int pinned);

    /** 更新帖子精华状态。 */
    int updatePostFeatured(@Param("id") Long id, @Param("featured") int featured);

    /** 删除帖子。 */
    int deletePost(@Param("id") Long id);

    /** 分页查询评论列表（管理视角）。 */
    List<AdminCommentVO> selectComments(@Param("keyword") String keyword,
                                        @Param("limit") int limit,
                                        @Param("offset") int offset);

    /** 查询评论总数（管理视角）。 */
    long countComments(@Param("keyword") String keyword);

    /** 删除评论。 */
    int deleteComment(@Param("id") Long id);
}
