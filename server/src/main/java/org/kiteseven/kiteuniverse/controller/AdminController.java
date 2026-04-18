package org.kiteseven.kiteuniverse.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.kiteseven.kiteuniverse.common.result.Result;
import org.kiteseven.kiteuniverse.pojo.dto.admin.ReportHandleDTO;
import org.kiteseven.kiteuniverse.pojo.dto.admin.UserMuteDTO;
import org.kiteseven.kiteuniverse.service.AdminService;
import org.kiteseven.kiteuniverse.support.auth.UserTokenService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理后台 API，所有操作均需 admin 角色。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserTokenService userTokenService;

    public AdminController(AdminService adminService, UserTokenService userTokenService) {
        this.adminService = adminService;
        this.userTokenService = userTokenService;
    }

    private Long resolveAdmin(HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        Long userId = userTokenService.parseUserId(token);
        adminService.requireAdmin(userId);
        return userId;
    }

    // ── Dashboard ────────────────────────────────────────────────

    @GetMapping("/stats")
    public Result<?> getStats(HttpServletRequest request) {
        resolveAdmin(request);
        return Result.success(adminService.getStats());
    }

    // ── User Management ──────────────────────────────────────────

    @GetMapping("/users")
    public Result<Map<String, Object>> listUsers(
            HttpServletRequest request,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        resolveAdmin(request);
        return Result.success(adminService.listUsers(keyword, status, page, limit));
    }

    @PutMapping("/users/{id}/ban")
    public Result<?> banUser(HttpServletRequest request, @PathVariable Long id) {
        resolveAdmin(request);
        adminService.banUser(id);
        return Result.success();
    }

    @PutMapping("/users/{id}/unban")
    public Result<?> unbanUser(HttpServletRequest request, @PathVariable Long id) {
        resolveAdmin(request);
        adminService.unbanUser(id);
        return Result.success();
    }

    @PutMapping("/users/{id}/mute")
    public Result<?> muteUser(HttpServletRequest request,
                              @PathVariable Long id,
                              @RequestBody UserMuteDTO dto) {
        resolveAdmin(request);
        adminService.muteUser(id, dto.getMinutes());
        return Result.success();
    }

    @PutMapping("/users/{id}/role")
    public Result<?> changeRole(HttpServletRequest request,
                                @PathVariable Long id,
                                @RequestBody Map<String, String> body) {
        resolveAdmin(request);
        adminService.changeRole(id, body.get("role"));
        return Result.success();
    }

    // ── Post Moderation ──────────────────────────────────────────

    @GetMapping("/posts")
    public Result<Map<String, Object>> listPosts(
            HttpServletRequest request,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        resolveAdmin(request);
        return Result.success(adminService.listPosts(keyword, status, page, limit));
    }

    @PutMapping("/posts/{id}/hide")
    public Result<?> hidePost(HttpServletRequest request, @PathVariable Long id) {
        resolveAdmin(request);
        adminService.hidePost(id);
        return Result.success();
    }

    @PutMapping("/posts/{id}/restore")
    public Result<?> restorePost(HttpServletRequest request, @PathVariable Long id) {
        resolveAdmin(request);
        adminService.restorePost(id);
        return Result.success();
    }

    @PutMapping("/posts/{id}/pin")
    public Result<?> pinPost(HttpServletRequest request, @PathVariable Long id) {
        resolveAdmin(request);
        adminService.pinPost(id);
        return Result.success();
    }

    @PutMapping("/posts/{id}/unpin")
    public Result<?> unpinPost(HttpServletRequest request, @PathVariable Long id) {
        resolveAdmin(request);
        adminService.unpinPost(id);
        return Result.success();
    }

    @PutMapping("/posts/{id}/feature")
    public Result<?> featurePost(HttpServletRequest request, @PathVariable Long id) {
        resolveAdmin(request);
        adminService.featurePost(id);
        return Result.success();
    }

    @PutMapping("/posts/{id}/unfeature")
    public Result<?> unfeaturePost(HttpServletRequest request, @PathVariable Long id) {
        resolveAdmin(request);
        adminService.unfeaturePost(id);
        return Result.success();
    }

    @DeleteMapping("/posts/{id}")
    public Result<?> deletePost(HttpServletRequest request, @PathVariable Long id) {
        resolveAdmin(request);
        adminService.deletePost(id);
        return Result.success();
    }

    // ── Comment Moderation ───────────────────────────────────────

    @GetMapping("/comments")
    public Result<Map<String, Object>> listComments(
            HttpServletRequest request,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        resolveAdmin(request);
        return Result.success(adminService.listComments(keyword, page, limit));
    }

    @DeleteMapping("/comments/{id}")
    public Result<?> deleteComment(HttpServletRequest request, @PathVariable Long id) {
        resolveAdmin(request);
        adminService.deleteComment(id);
        return Result.success();
    }

    // ── Report Management ────────────────────────────────────────

    @GetMapping("/reports")
    public Result<Map<String, Object>> listReports(
            HttpServletRequest request,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        resolveAdmin(request);
        return Result.success(adminService.listReports(status, page, limit));
    }

    @PutMapping("/reports/{id}/handle")
    public Result<?> handleReport(HttpServletRequest request,
                                  @PathVariable Long id,
                                  @RequestBody ReportHandleDTO dto) {
        Long adminId = resolveAdmin(request);
        adminService.handleReport(id, adminId, dto);
        return Result.success();
    }
}
