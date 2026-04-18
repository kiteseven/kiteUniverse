package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.GameAccount;

import java.util.List;

/**
 * 游戏账号数据访问接口。
 */
@Mapper
public interface GameAccountMapper {

    List<GameAccount> selectByUserId(@Param("userId") Long userId);

    GameAccount selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    GameAccount selectByUserIdAndGameUid(@Param("userId") Long userId, @Param("gameUid") String gameUid);

    int insert(GameAccount gameAccount);

    int updateByIdAndUserId(GameAccount gameAccount);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
