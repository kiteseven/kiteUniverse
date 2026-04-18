package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.ContentReport;
import org.kiteseven.kiteuniverse.pojo.vo.admin.AdminReportVO;

import java.util.List;

/**
 * 举报记录数据访问接口。
 */
@Mapper
public interface ContentReportMapper {

    /** 插入举报记录。 */
    int insert(ContentReport report);

    /** 分页查询举报列表。 */
    List<AdminReportVO> selectReports(@Param("status") Integer status,
                                      @Param("limit") int limit,
                                      @Param("offset") int offset);

    /** 查询举报总数。 */
    long countReports(@Param("status") Integer status);

    /** 按 ID 查询单条举报。 */
    ContentReport selectById(@Param("id") Long id);

    /** 更新举报处理状态。 */
    int updateHandle(@Param("id") Long id,
                     @Param("status") int status,
                     @Param("handlerId") Long handlerId,
                     @Param("handleNote") String handleNote);

    /** 查询待处理举报数（用于 Dashboard）。 */
    long countPending();
}
