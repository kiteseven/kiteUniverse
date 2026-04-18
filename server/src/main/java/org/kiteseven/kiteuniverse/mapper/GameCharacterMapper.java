package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.GameCharacterRecord;

import java.util.List;

/**
 * 游戏角色记录数据访问接口。
 */
@Mapper
public interface GameCharacterMapper {

    List<GameCharacterRecord> selectByUserId(@Param("userId") Long userId);

    GameCharacterRecord selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int insert(GameCharacterRecord record);

    int updateByIdAndUserId(GameCharacterRecord record);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
