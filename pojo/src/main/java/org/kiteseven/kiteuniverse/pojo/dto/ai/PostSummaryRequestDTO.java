package org.kiteseven.kiteuniverse.pojo.dto.ai;

/**
 * 请求 AI 生成帖子摘要的入参。
 */
public class PostSummaryRequestDTO {

    /**
     * 帖子编号（已发布帖子，可选；若不填则直接使用 content）。
     */
    private Long postId;

    /**
     * 帖子标题。
     */
    private String title;

    /**
     * 帖子正文（Markdown 格式）。
     */
    private String content;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
