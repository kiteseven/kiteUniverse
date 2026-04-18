package org.kiteseven.kiteuniverse.pojo.entity;

import org.kiteseven.kiteuniverse.pojo.base.BaseEntity;

import java.io.Serial;

/**
 * 用户举报记录实体类。
 */
public class ContentReport extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 举报人 ID。 */
    private Long reporterId;

    /** 举报目标类型：POST / COMMENT / USER。 */
    private String targetType;

    /** 举报目标 ID。 */
    private Long targetId;

    /** 举报原因分类。 */
    private String reason;

    /** 补充说明。 */
    private String description;

    /** 状态：0=待处理，1=已处理（采纳），2=已处理（驳回）。 */
    private Integer status;

    /** 处理管理员 ID。 */
    private Long handlerId;

    /** 处理备注。 */
    private String handleNote;

    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Long getHandlerId() { return handlerId; }
    public void setHandlerId(Long handlerId) { this.handlerId = handlerId; }

    public String getHandleNote() { return handleNote; }
    public void setHandleNote(String handleNote) { this.handleNote = handleNote; }
}
