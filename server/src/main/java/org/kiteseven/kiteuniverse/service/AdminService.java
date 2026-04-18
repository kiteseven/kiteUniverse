package org.kiteseven.kiteuniverse.service;

import org.kiteseven.kiteuniverse.pojo.vo.admin.AdminStatsVO;
import org.kiteseven.kiteuniverse.pojo.dto.admin.ReportHandleDTO;

import java.util.Map;

/**
 * 管理后台业务服务接口。
 */
public interface AdminService {

    /** 验证当前用户是否为管理员，不是则抛出异常。 */
    void requireAdmin(Long userId);

    /** 获取数据统计面板。 */
    AdminStatsVO getStats();

    /** 分页查询用户列表。 */
    Map<String, Object> listUsers(String keyword, Integer status, int page, int limit);

    /** 封禁用户。 */
    void banUser(Long targetId);

    /** 解封用户。 */
    void unbanUser(Long targetId);

    /** 禁言用户（minutes=0 表示解除）。 */
    void muteUser(Long targetId, int minutes);

    /** 修改用户角色。 */
    void changeRole(Long targetId, String role);

    /** 分页查询帖子列表（管理视角）。 */
    Map<String, Object> listPosts(String keyword, Integer status, int page, int limit);

    /** 隐藏帖子。 */
    void hidePost(Long postId);

    /** 恢复帖子。 */
    void restorePost(Long postId);

    /** 置顶帖子。 */
    void pinPost(Long postId);

    /** 取消置顶帖子。 */
    void unpinPost(Long postId);

    /** 设为精华帖子。 */
    void featurePost(Long postId);

    /** 取消精华帖子。 */
    void unfeaturePost(Long postId);

    /** 删除帖子。 */
    void deletePost(Long postId);

    /** 分页查询评论列表（管理视角）。 */
    Map<String, Object> listComments(String keyword, int page, int limit);

    /** 删除评论。 */
    void deleteComment(Long commentId);

    /** 分页查询举报列表。 */
    Map<String, Object> listReports(Integer status, int page, int limit);

    /** 处理举报。 */
    void handleReport(Long reportId, Long handlerId, ReportHandleDTO dto);
}
