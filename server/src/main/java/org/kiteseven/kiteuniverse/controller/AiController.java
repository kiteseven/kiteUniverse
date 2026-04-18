package org.kiteseven.kiteuniverse.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.result.Result;
import org.kiteseven.kiteuniverse.pojo.dto.ai.BadgeSuggestRequestDTO;
import org.kiteseven.kiteuniverse.pojo.dto.ai.PostSummaryRequestDTO;
import org.kiteseven.kiteuniverse.pojo.vo.ai.AiBadgeSuggestVO;
import org.kiteseven.kiteuniverse.pojo.vo.ai.AiGrowthReportVO;
import org.kiteseven.kiteuniverse.pojo.vo.ai.AiPostSummaryVO;
import org.kiteseven.kiteuniverse.service.AiService;
import org.kiteseven.kiteuniverse.support.auth.UserTokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 辅助功能接口，提供摘要生成、标签推荐、成长报告三个端点。
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;
    private final UserTokenService userTokenService;

    public AiController(AiService aiService, UserTokenService userTokenService) {
        this.aiService = aiService;
        this.userTokenService = userTokenService;
    }

    /**
     * 根据帖子标题和正文生成摘要（最多 100 字）。
     * 无需登录，发帖前预览可匿名调用。
     */
    @PostMapping("/post-summary")
    public Result<AiPostSummaryVO> generatePostSummary(@RequestBody PostSummaryRequestDTO dto) {
        AiPostSummaryVO vo = aiService.generatePostSummary(dto.getTitle(), dto.getContent());
        if (dto.getPostId() != null) {
            vo.setPostId(dto.getPostId());
        }
        return Result.success(vo);
    }

    /**
     * 根据帖子标题和正文推荐分类标签（3–5 个）。
     * 无需登录，适合发帖时实时调用。
     */
    @PostMapping("/badge-suggest")
    public Result<AiBadgeSuggestVO> suggestBadges(@RequestBody BadgeSuggestRequestDTO dto) {
        return Result.success(aiService.suggestBadges(dto.getTitle(), dto.getContent()));
    }

    /**
     * 为当前登录用户生成个性化玩家成长报告。
     * 需要登录。
     */
    @PostMapping("/growth-report")
    public Result<AiGrowthReportVO> generateGrowthReport(HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        Long userId = userTokenService.parseUserId(token);
        return Result.success(aiService.generateGrowthReport(userId));
    }
}
