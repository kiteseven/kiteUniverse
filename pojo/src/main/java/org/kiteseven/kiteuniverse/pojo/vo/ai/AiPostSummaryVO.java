package org.kiteseven.kiteuniverse.pojo.vo.ai;

/**
 * AI 生成的帖子摘要结果。
 */
public class AiPostSummaryVO {

    /**
     * 帖子编号。
     */
    private Long postId;

    /**
     * AI 生成的摘要（100 字以内）。
     */
    private String summary;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
