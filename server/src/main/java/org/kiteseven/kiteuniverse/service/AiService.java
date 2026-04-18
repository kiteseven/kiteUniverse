package org.kiteseven.kiteuniverse.service;

import org.kiteseven.kiteuniverse.pojo.vo.ai.AiBadgeSuggestVO;
import org.kiteseven.kiteuniverse.pojo.vo.ai.AiGrowthReportVO;
import org.kiteseven.kiteuniverse.pojo.vo.ai.AiPostSummaryVO;

/**
 * AI 辅助功能服务接口，提供摘要生成、标签推荐、成长报告三项能力。
 */
public interface AiService {

    /**
     * 根据帖子标题和正文生成 100 字以内的中文摘要。
     *
     * @param title   帖子标题
     * @param content 帖子正文（Markdown 格式）
     * @return 摘要结果
     */
    AiPostSummaryVO generatePostSummary(String title, String content);

    /**
     * 根据帖子标题和正文推荐 3–5 个分类标签。
     *
     * @param title   帖子标题
     * @param content 帖子正文（Markdown 格式）
     * @return 标签推荐结果
     */
    AiBadgeSuggestVO suggestBadges(String title, String content);

    /**
     * 根据用户行为数据（积分、等级、发帖量等）生成个性化玩家成长报告。
     *
     * @param userId 用户编号
     * @return 成长报告结果
     */
    AiGrowthReportVO generateGrowthReport(Long userId);
}
