package org.kiteseven.kiteuniverse.pojo.entity;

import org.kiteseven.kiteuniverse.pojo.base.BaseEntity;

import java.io.Serial;

/**
 * 社区版块实体类。
 */
public class CommunityBoard extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 版块名称。
     */
    private String name;

    /**
     * 版块别名，用于路由或链接拼接。
     */
    private String slug;

    /**
     * 版块标签名称。
     */
    private String tagName;

    /**
     * 版块简介。
     */
    private String description;

    /**
     * 版块排序值，越小越靠前。
     */
    private Integer sortOrder;

    /**
     * 版块状态，1 表示启用。
     */
    private Integer status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
