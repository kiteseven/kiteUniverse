package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.UserPoints;

/**
 * 用户积分数据访问接口。
 */
@Mapper
public interface UserPointsMapper {

    UserPoints selectByUserId(@Param("userId") Long userId);

    int insert(UserPoints userPoints);

    int update(UserPoints userPoints);
}
