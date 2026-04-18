package org.kiteseven.kiteuniverse.pojo.dto.community;

/**
 * 评论请求对象。
 */
public class PostCommentCreateDTO {

    /**
     * 评论内容。
     */
    private String content;

    /**
     * 父评论编号（null 表示顶层评论）。
     */
    private Long parentId;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
