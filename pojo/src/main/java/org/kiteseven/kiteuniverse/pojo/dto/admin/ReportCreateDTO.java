package org.kiteseven.kiteuniverse.pojo.dto.admin;

/**
 * 提交举报请求 DTO。
 */
public class ReportCreateDTO {

    /** 举报目标类型：POST / COMMENT / USER。 */
    private String targetType;

    /** 举报目标 ID。 */
    private Long targetId;

    /** 举报原因分类。 */
    private String reason;

    /** 补充说明，可选。 */
    private String description;

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
