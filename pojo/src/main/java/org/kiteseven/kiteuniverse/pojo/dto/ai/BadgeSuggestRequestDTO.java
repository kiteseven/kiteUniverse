package org.kiteseven.kiteuniverse.pojo.dto.ai;

/**
 * 请求 AI 推荐帖子标签的入参。
 */
public class BadgeSuggestRequestDTO {

    /**
     * 帖子标题。
     */
    private String title;

    /**
     * 帖子正文（Markdown 格式）。
     */
    private String content;

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
