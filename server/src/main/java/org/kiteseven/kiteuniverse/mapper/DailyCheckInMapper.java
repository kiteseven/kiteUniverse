package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.DailyCheckIn;

import java.time.LocalDate;

/**
 * 每日签到记录数据访问接口。
 */
@Mapper
public interface DailyCheckInMapper {

    DailyCheckIn selectByUserIdAndDate(@Param("userId") Long userId, @Param("checkInDate") LocalDate checkInDate);

    int insert(DailyCheckIn dailyCheckIn);
}
