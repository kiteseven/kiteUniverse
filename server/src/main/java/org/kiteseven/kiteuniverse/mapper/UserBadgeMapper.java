package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.UserBadge;

import java.util.List;

/**
 * 用户徽章数据访问接口。
 */
@Mapper
public interface UserBadgeMapper {

    List<UserBadge> selectByUserId(@Param("userId") Long userId);

    UserBadge selectByUserIdAndType(@Param("userId") Long userId, @Param("badgeType") String badgeType);

    int insert(UserBadge userBadge);
}
