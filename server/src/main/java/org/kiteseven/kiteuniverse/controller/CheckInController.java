package org.kiteseven.kiteuniverse.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.result.Result;
import org.kiteseven.kiteuniverse.pojo.vo.checkin.CheckInResultVO;
import org.kiteseven.kiteuniverse.pojo.vo.checkin.CheckInStatusVO;
import org.kiteseven.kiteuniverse.pojo.vo.checkin.UserBadgeVO;
import org.kiteseven.kiteuniverse.service.CheckInService;
import org.kiteseven.kiteuniverse.support.auth.UserTokenService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 签到与激励接口。
 */
@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService checkInService;
    private final UserTokenService userTokenService;

    public CheckInController(CheckInService checkInService, UserTokenService userTokenService) {
        this.checkInService = checkInService;
        this.userTokenService = userTokenService;
    }

    /** 执行今日签到。 */
    @PostMapping
    public Result<CheckInResultVO> checkIn(HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        Long userId = userTokenService.parseUserId(token);
        return Result.success(checkInService.checkIn(userId));
    }

    /** 查询今日签到状态及积分概览。 */
    @GetMapping("/status")
    public Result<CheckInStatusVO> getStatus(HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        Long userId = userTokenService.parseUserId(token);
        return Result.success(checkInService.getStatus(userId));
    }

    /** 查询当前用户的所有徽章。 */
    @GetMapping("/badges")
    public Result<List<UserBadgeVO>> getBadges(HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        Long userId = userTokenService.parseUserId(token);
        return Result.success(checkInService.getBadges(userId));
    }
}
