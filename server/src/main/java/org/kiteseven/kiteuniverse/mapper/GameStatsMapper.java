package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.GameStats;

/**
 * 游戏数据快照数据访问接口。
 */
@Mapper
public interface GameStatsMapper {

    GameStats selectByUserId(@Param("userId") Long userId);

    int insert(GameStats stats);

    int updateByUserId(GameStats stats);
}
