package org.kiteseven.kiteuniverse.pojo.vo.community;

import java.time.LocalDateTime;

/**
 * 版块概要视图对象。
 */
public class BoardSummaryVO {

    /**
     * 版块编号。
     */
    private Long id;

    /**
     * 版块名称。
     */
    private String name;

    /**
     * 版块别名。
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
     * 版块帖子总数。
     */
    private Long topicCount;

    /**
     * 今日新增帖子数。
     */
    private Long todayPostCount;

    /**
     * 最近一条帖子的标题。
     */
    private String latestPostTitle;

    /**
     * 最近一条帖子的发布时间。
     */
    private LocalDateTime latestPublishedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getTopicCount() {
        return topicCount;
    }

    public void setTopicCount(Long topicCount) {
        this.topicCount = topicCount;
    }

    public Long getTodayPostCount() {
        return todayPostCount;
    }

    public void setTodayPostCount(Long todayPostCount) {
        this.todayPostCount = todayPostCount;
    }

    public String getLatestPostTitle() {
        return latestPostTitle;
    }

    public void setLatestPostTitle(String latestPostTitle) {
        this.latestPostTitle = latestPostTitle;
    }

    public LocalDateTime getLatestPublishedAt() {
        return latestPublishedAt;
    }

    public void setLatestPublishedAt(LocalDateTime latestPublishedAt) {
        this.latestPublishedAt = latestPublishedAt;
    }
}
