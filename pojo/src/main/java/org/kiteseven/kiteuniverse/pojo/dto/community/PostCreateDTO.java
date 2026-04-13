package org.kiteseven.kiteuniverse.pojo.dto.community;

/**
 * 发帖请求对象。
 */
public class PostCreateDTO {

    /**
     * 所属版块编号。
     */
    private Long boardId;

    /**
     * 帖子标题。
     */
    private String title;

    /**
     * 帖子摘要。
     */
    private String summary;

    /**
     * 帖子正文。
     */
    private String content;

    /**
     * 可选徽标文案。
     */
    private String badge;

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }
}
