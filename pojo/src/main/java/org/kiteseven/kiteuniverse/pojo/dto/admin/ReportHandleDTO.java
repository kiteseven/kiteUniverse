package org.kiteseven.kiteuniverse.pojo.dto.admin;

/**
 * 处理举报请求 DTO。
 */
public class ReportHandleDTO {

    /** 处理动作：approve（采纳并删除内容）/ dismiss（驳回举报）。 */
    private String action;

    /** 处理备注，可选。 */
    private String note;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
