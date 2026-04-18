package org.kiteseven.kiteuniverse.service.impl;

import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.mapper.ContentReportMapper;
import org.kiteseven.kiteuniverse.pojo.dto.admin.ReportCreateDTO;
import org.kiteseven.kiteuniverse.pojo.entity.ContentReport;
import org.kiteseven.kiteuniverse.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * 用户举报业务实现。
 */
@Service
public class ReportServiceImpl implements ReportService {

    private static final Set<String> VALID_TYPES = Set.of("POST", "COMMENT", "USER");
    private static final Set<String> VALID_REASONS = Set.of(
            "spam", "harassment", "inappropriate", "misinformation", "other"
    );

    private final ContentReportMapper contentReportMapper;

    public ReportServiceImpl(ContentReportMapper contentReportMapper) {
        this.contentReportMapper = contentReportMapper;
    }

    @Override
    public void submitReport(Long reporterId, ReportCreateDTO dto) {
        if (dto.getTargetId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "举报目标不能为空");
        }
        if (!VALID_TYPES.contains(dto.getTargetType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "举报类型无效");
        }
        if (!StringUtils.hasText(dto.getReason()) || !VALID_REASONS.contains(dto.getReason())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请选择举报原因");
        }

        ContentReport report = new ContentReport();
        report.setReporterId(reporterId);
        report.setTargetType(dto.getTargetType());
        report.setTargetId(dto.getTargetId());
        report.setReason(dto.getReason());
        report.setDescription(StringUtils.hasText(dto.getDescription()) ? dto.getDescription().trim() : "");
        report.setStatus(0);
        contentReportMapper.insert(report);
    }
}
