package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityBoard;
import org.kiteseven.kiteuniverse.pojo.vo.community.BoardSummaryVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 社区版块数据访问接口。
 */
@Mapper
public interface CommunityBoardMapper {

    /**
     * 统计版块总数。
     *
     * @return 版块数量
     */
    long countAll();

    /**
     * 按别名查询版块。
     *
     * @param slug 版块别名
     * @return 版块实体
     */
    CommunityBoard selectBySlug(@Param("slug") String slug);

    /**
     * 按编号查询版块。
     *
     * @param id 版块编号
     * @return 版块实体
     */
    CommunityBoard selectById(@Param("id") Long id);

    /**
     * 查询所有启用版块。
     *
     * @return 版块列表
     */
    List<CommunityBoard> selectActiveBoards();

    /**
     * 查询版块概要列表，并聚合帖子统计信息。
     *
     * @param todayStart 今日零点时间
     * @return 版块概要列表
     */
    List<BoardSummaryVO> selectActiveBoardSummaries(@Param("todayStart") LocalDateTime todayStart);

    /**
     * 按编号查询版块概要信息。
     *
     * @param id 版块编号
     * @param todayStart 今日零点时间
     * @return 版块概要
     */
    BoardSummaryVO selectBoardSummaryById(@Param("id") Long id, @Param("todayStart") LocalDateTime todayStart);

    /**
     * 新增版块记录。
     *
     * @param communityBoard 版块实体
     * @return 影响行数
     */
    int insert(CommunityBoard communityBoard);
}
