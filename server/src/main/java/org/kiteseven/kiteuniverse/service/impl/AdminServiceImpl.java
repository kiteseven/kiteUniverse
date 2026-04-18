package org.kiteseven.kiteuniverse.service.impl;

import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.mapper.AdminMapper;
import org.kiteseven.kiteuniverse.mapper.ContentReportMapper;
import org.kiteseven.kiteuniverse.mapper.UserMapper;
import org.kiteseven.kiteuniverse.pojo.dto.admin.ReportHandleDTO;
import org.kiteseven.kiteuniverse.pojo.entity.ContentReport;
import org.kiteseven.kiteuniverse.pojo.entity.User;
import org.kiteseven.kiteuniverse.pojo.vo.admin.AdminStatsVO;
import org.kiteseven.kiteuniverse.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台业务实现。
 */
@Service
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final AdminMapper adminMapper;
    private final ContentReportMapper contentReportMapper;

    public AdminServiceImpl(UserMapper userMapper,
                            AdminMapper adminMapper,
                            ContentReportMapper contentReportMapper) {
        this.userMapper = userMapper;
        this.adminMapper = adminMapper;
        this.contentReportMapper = contentReportMapper;
    }

    @Override
    public void requireAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !"admin".equals(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "需要管理员权限");
        }
    }

    @Override
    public AdminStatsVO getStats() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        AdminStatsVO vo = new AdminStatsVO();
        vo.setTotalUsers(userMapper.countAll());
        vo.setNewUsersToday(userMapper.countCreatedSince(todayStart));
        vo.setActiveUsersToday(userMapper.countLastLoginSince(todayStart));
        vo.setTotalPosts(adminMapper.countAllPosts());
        vo.setNewPostsToday(adminMapper.countPostsCreatedSince(todayStart));
        vo.setTotalComments(adminMapper.countAllComments());
        vo.setPendingReports(contentReportMapper.countPending());
        vo.setBannedUsers(adminMapper.countBannedUsers());
        return vo;
    }

    @Override
    public Map<String, Object> listUsers(String keyword, Integer status, int page, int limit) {
        int offset = page * limit;
        Map<String, Object> result = new HashMap<>();
        result.put("items", adminMapper.selectUsers(keyword, status, limit, offset));
        result.put("total", adminMapper.countUsers(keyword, status));
        return result;
    }

    @Override
    public void banUser(Long targetId) {
        adminMapper.updateUserStatus(targetId, 0);
    }

    @Override
    public void unbanUser(Long targetId) {
        adminMapper.updateUserStatus(targetId, 1);
    }

    @Override
    public void muteUser(Long targetId, int minutes) {
        LocalDateTime muteUntil = minutes > 0
                ? LocalDateTime.now().plusMinutes(minutes)
                : null;
        adminMapper.updateUserMuteUntil(targetId, muteUntil);
    }

    @Override
    public void changeRole(Long targetId, String role) {
        if (!"admin".equals(role) && !"user".equals(role)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的角色值");
        }
        adminMapper.updateUserRole(targetId, role);
    }

    @Override
    public Map<String, Object> listPosts(String keyword, Integer status, int page, int limit) {
        int offset = page * limit;
        Map<String, Object> result = new HashMap<>();
        result.put("items", adminMapper.selectPosts(keyword, status, limit, offset));
        result.put("total", adminMapper.countPosts(keyword, status));
        return result;
    }

    @Override
    public void hidePost(Long postId) {
        adminMapper.updatePostStatus(postId, 0);
    }

    @Override
    public void restorePost(Long postId) {
        adminMapper.updatePostStatus(postId, 1);
    }

    @Override
    public void pinPost(Long postId) {
        adminMapper.updatePostPinned(postId, 1);
    }

    @Override
    public void unpinPost(Long postId) {
        adminMapper.updatePostPinned(postId, 0);
    }

    @Override
    public void featurePost(Long postId) {
        adminMapper.updatePostFeatured(postId, 1);
    }

    @Override
    public void unfeaturePost(Long postId) {
        adminMapper.updatePostFeatured(postId, 0);
    }

    @Override
    public void deletePost(Long postId) {
        adminMapper.deletePost(postId);
    }

    @Override
    public Map<String, Object> listComments(String keyword, int page, int limit) {
        int offset = page * limit;
        Map<String, Object> result = new HashMap<>();
        result.put("items", adminMapper.selectComments(keyword, limit, offset));
        result.put("total", adminMapper.countComments(keyword));
        return result;
    }

    @Override
    public void deleteComment(Long commentId) {
        adminMapper.deleteComment(commentId);
    }

    @Override
    public Map<String, Object> listReports(Integer status, int page, int limit) {
        int offset = page * limit;
        Map<String, Object> result = new HashMap<>();
        result.put("items", contentReportMapper.selectReports(status, limit, offset));
        result.put("total", contentReportMapper.countReports(status));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleReport(Long reportId, Long handlerId, ReportHandleDTO dto) {
        ContentReport report = contentReportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "举报记录不存在");
        }
        if (report.getStatus() != 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该举报已处理");
        }

        boolean approve = "approve".equals(dto.getAction());
        int newStatus = approve ? 1 : 2;
        contentReportMapper.updateHandle(reportId, newStatus, handlerId,
                dto.getNote() != null ? dto.getNote() : "");

        if (approve) {
            switch (report.getTargetType()) {
                case "POST" -> adminMapper.deletePost(report.getTargetId());
                case "COMMENT" -> adminMapper.deleteComment(report.getTargetId());
                default -> { /* USER type: no auto action */ }
            }
        }
    }
}
