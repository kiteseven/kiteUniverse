package org.kiteseven.kiteuniverse.service.impl;

import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.mapper.CommunityPostMapper;
import org.kiteseven.kiteuniverse.mapper.UserPointsMapper;
import org.kiteseven.kiteuniverse.pojo.entity.UserPoints;
import org.kiteseven.kiteuniverse.pojo.vo.ai.AiBadgeSuggestVO;
import org.kiteseven.kiteuniverse.pojo.vo.ai.AiGrowthReportVO;
import org.kiteseven.kiteuniverse.pojo.vo.ai.AiPostSummaryVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostSummaryVO;
import org.kiteseven.kiteuniverse.service.AiService;
import org.kiteseven.kiteuniverse.support.ai.DeepSeekClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 功能服务实现，对接 DeepSeek（OpenAI 协议兼容）大模型完成三类推理任务。
 */
@Service
public class AiServiceImpl implements AiService {

    /** 等级名称与 CheckInServiceImpl 保持一致。 */
    private static final String[] LEVEL_NAMES = {"探索者", "初入回廊", "深渊行者", "虚空老兵", "裂隙征服者", "虚空主宰"};

    private final DeepSeekClient deepSeekClient;
    private final UserPointsMapper userPointsMapper;
    private final CommunityPostMapper communityPostMapper;

    public AiServiceImpl(DeepSeekClient deepSeekClient,
                         UserPointsMapper userPointsMapper,
                         CommunityPostMapper communityPostMapper) {
        this.deepSeekClient = deepSeekClient;
        this.userPointsMapper = userPointsMapper;
        this.communityPostMapper = communityPostMapper;
    }

    @Override
    public AiPostSummaryVO generatePostSummary(String title, String content) {
        if (!StringUtils.hasText(title) && !StringUtils.hasText(content)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "标题和内容不能同时为空");
        }

        String prompt = buildSummaryPrompt(title, content);
        String summary = deepSeekClient.chat(prompt).trim();

        // 截断到 100 字
        if (summary.length() > 100) {
            summary = summary.substring(0, 99) + "…";
        }

        AiPostSummaryVO vo = new AiPostSummaryVO();
        vo.setSummary(summary);
        return vo;
    }

    @Override
    public AiBadgeSuggestVO suggestBadges(String title, String content) {
        if (!StringUtils.hasText(title) && !StringUtils.hasText(content)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "标题和内容不能同时为空");
        }

        String prompt = buildBadgePrompt(title, content);
        String raw = deepSeekClient.chat(prompt).trim();

        // 解析：期望模型输出 "标签1,标签2,标签3|推荐理由" 格式
        List<String> badges;
        String reason;
        if (raw.contains("|")) {
            String[] parts = raw.split("\\|", 2);
            badges = parseBadges(parts[0]);
            reason = parts[1].trim();
        } else {
            badges = parseBadges(raw);
            reason = "基于帖子内容自动推荐";
        }

        AiBadgeSuggestVO vo = new AiBadgeSuggestVO();
        vo.setBadges(badges);
        vo.setReason(reason);
        return vo;
    }

    @Override
    public AiGrowthReportVO generateGrowthReport(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户编号不能为空");
        }

        UserPoints userPoints = userPointsMapper.selectByUserId(userId);
        int points = userPoints != null ? userPoints.getPoints() : 0;
        int level = userPoints != null ? Math.max(1, Math.min(userPoints.getLevel(), 6)) : 1;
        int consecutive = userPoints != null ? userPoints.getConsecutiveDays() : 0;
        int totalEarned = userPoints != null ? userPoints.getTotalPointsEarned() : 0;
        String levelName = LEVEL_NAMES[level - 1];

        List<PostSummaryVO> posts = communityPostMapper.selectPostsByAuthorId(userId, 100);
        int postCount = posts != null ? posts.size() : 0;

        String prompt = buildGrowthReportPrompt(levelName, points, totalEarned, consecutive, postCount);
        String report = deepSeekClient.chat(prompt).trim();

        AiGrowthReportVO vo = new AiGrowthReportVO();
        vo.setUserId(userId);
        vo.setLevelName(levelName);
        vo.setPoints(points);
        vo.setReport(report);
        return vo;
    }

    // ──────────────────────────── Prompt builders ─────────────────────────────

    private String buildSummaryPrompt(String title, String content) {
        String truncatedContent = content != null && content.length() > 2000
                ? content.substring(0, 2000) + "…"
                : (content != null ? content : "");
        return "你是一位专业的游戏社区编辑。请根据以下帖子内容，用简洁流畅的中文生成一段不超过 100 字的摘要。" +
                "只输出摘要正文，不要包含任何解释或标题。\n\n" +
                "【帖子标题】" + (StringUtils.hasText(title) ? title : "（无标题）") + "\n\n" +
                "【帖子内容】\n" + truncatedContent;
    }

    private String buildBadgePrompt(String title, String content) {
        String truncatedContent = content != null && content.length() > 1500
                ? content.substring(0, 1500) + "…"
                : (content != null ? content : "");
        return "你是一位游戏社区标签管理员。请根据以下帖子为其推荐 3 到 5 个简短的中文分类标签（每个标签不超过 6 个字），" +
                "标签之间用英文逗号分隔，然后用竖线 | 分隔推荐理由（不超过 50 字）。\n" +
                "输出格式示例：攻略分享,新手引导,地图探索|这篇帖子包含新手攻略和地图探索内容，适合以上标签。\n\n" +
                "【帖子标题】" + (StringUtils.hasText(title) ? title : "（无标题）") + "\n\n" +
                "【帖子内容】\n" + truncatedContent;
    }

    private String buildGrowthReportPrompt(String levelName, int points, int totalEarned,
                                           int consecutive, int postCount) {
        return "你是 KiteUniverse 游戏社区的 AI 玩家成长顾问。请根据以下玩家数据，用温暖鼓励的语气生成一份个性化的「玩家成长报告」，" +
                "使用 Markdown 格式，包含：成就总结、优势亮点、成长建议三个部分，总字数控制在 300 字以内。\n\n" +
                "【玩家数据】\n" +
                "- 当前等级：" + levelName + "\n" +
                "- 当前积分：" + points + " 分\n" +
                "- 累计获得积分：" + totalEarned + " 分\n" +
                "- 连续签到天数：" + consecutive + " 天\n" +
                "- 发帖数量：" + postCount + " 篇\n";
    }

    private List<String> parseBadges(String raw) {
        return Arrays.stream(raw.split("[,，]"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .limit(5)
                .collect(Collectors.toList());
    }
}
