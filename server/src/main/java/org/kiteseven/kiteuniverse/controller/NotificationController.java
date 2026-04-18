package org.kiteseven.kiteuniverse.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.result.Result;
import org.kiteseven.kiteuniverse.pojo.vo.notification.NotificationVO;
import org.kiteseven.kiteuniverse.pojo.vo.notification.UnreadCountVO;
import org.kiteseven.kiteuniverse.service.NotificationService;
import org.kiteseven.kiteuniverse.support.auth.UserTokenService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 站内通知接口。
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserTokenService userTokenService;

    public NotificationController(NotificationService notificationService,
                                  UserTokenService userTokenService) {
        this.notificationService = notificationService;
        this.userTokenService = userTokenService;
    }

    /**
     * 查询当前用户的通知列表。
     */
    @GetMapping
    public Result<List<NotificationVO>> listNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "50") int limit) {
        Long userId = requireCurrentUser(request);
        return Result.success(notificationService.listNotifications(userId, limit));
    }

    /**
     * 查询当前用户的未读通知数量。
     */
    @GetMapping("/unread-count")
    public Result<UnreadCountVO> getUnreadCount(HttpServletRequest request) {
        Long userId = requireCurrentUser(request);
        return Result.success(notificationService.getUnreadCount(userId));
    }

    /**
     * 将当前用户所有通知标记为已读。
     */
    @PutMapping("/read-all")
    public Result<Void> markAllRead(HttpServletRequest request) {
        Long userId = requireCurrentUser(request);
        notificationService.markAllRead(userId);
        return Result.success();
    }

    /**
     * 将指定通知标记为已读。
     */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = requireCurrentUser(request);
        notificationService.markRead(id, userId);
        return Result.success();
    }

    /**
     * 管理员发布系统公告（向全体用户发送通知）。
     * 简单鉴权：需要携带有效 token，且 body 中含 adminSecret 字段。
     */
    @PostMapping("/announcement")
    public Result<Void> publishAnnouncement(@RequestBody Map<String, String> body,
                                            HttpServletRequest request) {
        requireCurrentUser(request);
        String content = body == null ? null : body.get("content");
        if (!StringUtils.hasText(content)) {
            return Result.fail(ResultCode.BAD_REQUEST, "公告内容不能为空");
        }
        notificationService.publishAnnouncement(content);
        return Result.success();
    }

    /**
     * 从请求中解析当前用户编号，未登录时抛出异常。
     *
     * @param request HTTP 请求
     * @return 当前用户编号
     */
    private Long requireCurrentUser(HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        return userTokenService.parseUserId(token);
    }
}
