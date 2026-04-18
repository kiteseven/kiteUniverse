package org.kiteseven.kiteuniverse.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.kiteseven.kiteuniverse.common.result.Result;
import org.kiteseven.kiteuniverse.pojo.dto.admin.ReportCreateDTO;
import org.kiteseven.kiteuniverse.service.ReportService;
import org.kiteseven.kiteuniverse.support.auth.UserTokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户举报 API，需登录。
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserTokenService userTokenService;

    public ReportController(ReportService reportService, UserTokenService userTokenService) {
        this.reportService = reportService;
        this.userTokenService = userTokenService;
    }

    @PostMapping
    public Result<?> submitReport(HttpServletRequest request, @RequestBody ReportCreateDTO dto) {
        String token = userTokenService.resolveToken(request);
        Long userId = userTokenService.parseUserId(token);
        reportService.submitReport(userId, dto);
        return Result.success();
    }
}
