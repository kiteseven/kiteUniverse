package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.UserInfo;

/**
 * 用户资料数据访问接口。
 */
@Mapper
public interface UserInfoMapper {

    /**
     * 根据用户ID查询资料信息。
     *
     * @param userId 用户ID
     * @return 用户资料信息
     */
    UserInfo selectByUserId(@Param("userId") Long userId);

    /**
     * 新增用户资料。
     *
     * @param userInfo 用户资料实体
     * @return 影响行数
     */
    int insert(UserInfo userInfo);

    /**
     * 根据用户ID更新资料。
     *
     * @param userInfo 用户资料实体
     * @return 影响行数
     */
    int updateByUserId(UserInfo userInfo);
}
