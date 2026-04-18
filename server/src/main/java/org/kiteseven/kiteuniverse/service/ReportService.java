package org.kiteseven.kiteuniverse.service;

import org.kiteseven.kiteuniverse.pojo.dto.admin.ReportCreateDTO;

/**
 * 用户举报业务服务接口。
 */
public interface ReportService {

    /** 提交举报。 */
    void submitReport(Long reporterId, ReportCreateDTO dto);
}
