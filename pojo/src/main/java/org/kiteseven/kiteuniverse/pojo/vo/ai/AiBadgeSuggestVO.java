package org.kiteseven.kiteuniverse.pojo.vo.ai;

import java.util.List;

/**
 * AI 生成的帖子标签建议结果。
 */
public class AiBadgeSuggestVO {

    /**
     * 建议的标签列表（最多 5 个）。
     */
    private List<String> badges;

    /**
     * 推荐理由简述。
     */
    private String reason;

    public List<String> getBadges() {
        return badges;
    }

    public void setBadges(List<String> badges) {
        this.badges = badges;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
