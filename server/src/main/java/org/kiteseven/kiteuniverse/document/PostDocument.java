package org.kiteseven.kiteuniverse.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;

/**
 * Elasticsearch 帖子文档，对应索引 {@code community_posts}。
 *
 * <p>文本字段采用 IK 中文分词器：
 * <ul>
 *   <li>索引时：{@code ik_max_word}（细粒度分词，提高召回率）</li>
 *   <li>搜索时：{@code ik_smart}（粗粒度分词，提高精确度）</li>
 * </ul>
 */
@Document(indexName = "community_posts")
@Setting(settingPath = "es/community-posts-settings.json")
public class PostDocument {

    @Id
    private Long id;

    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
                otherFields = {
                    @InnerField(suffix = "pinyin", type = FieldType.Text,
                                analyzer = "ik_pinyin_analyzer", searchAnalyzer = "pinyin_analyzer")
                })
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String summary;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
                otherFields = {
                    @InnerField(suffix = "pinyin", type = FieldType.Text,
                                analyzer = "ik_pinyin_analyzer", searchAnalyzer = "pinyin_analyzer")
                })
    private String badge;

    @Field(type = FieldType.Keyword)
    private Long boardId;

    @Field(type = FieldType.Keyword)
    private String boardName;

    @Field(type = FieldType.Keyword)
    private String boardSlug;

    @Field(type = FieldType.Keyword)
    private String boardTagName;

    @Field(type = FieldType.Keyword)
    private Long authorId;

    @Field(type = FieldType.Keyword)
    private String authorName;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Integer)
    private Integer viewCount;

    @Field(type = FieldType.Integer)
    private Integer commentCount;

    @Field(type = FieldType.Integer)
    private Integer favoriteCount;

    @Field(type = FieldType.Integer)
    private Integer likeCount;

    @Field(type = FieldType.Boolean)
    private Boolean pinned;

    @Field(type = FieldType.Boolean)
    private Boolean featured;

    @Field(type = FieldType.Boolean)
    private Boolean aiGenerated;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime publishedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }

    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }

    public String getBoardName() { return boardName; }
    public void setBoardName(String boardName) { this.boardName = boardName; }

    public String getBoardSlug() { return boardSlug; }
    public void setBoardSlug(String boardSlug) { this.boardSlug = boardSlug; }

    public String getBoardTagName() { return boardTagName; }
    public void setBoardTagName(String boardTagName) { this.boardTagName = boardTagName; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }

    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Boolean getPinned() { return pinned; }
    public void setPinned(Boolean pinned) { this.pinned = pinned; }

    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }

    public Boolean getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(Boolean aiGenerated) { this.aiGenerated = aiGenerated; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
