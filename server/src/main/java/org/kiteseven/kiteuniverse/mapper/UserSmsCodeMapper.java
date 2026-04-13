package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.UserSmsCode;

import java.time.LocalDateTime;

/**
 * Data access object for phone verification codes.
 */
@Mapper
public interface UserSmsCodeMapper {

    /**
     * Inserts a new verification code record.
     *
     * @param userSmsCode code entity
     * @return affected row count
     */
    int insert(UserSmsCode userSmsCode);

    /**
     * Finds the latest unused verification code under the specified scene.
     *
     * @param phone mobile phone number
     * @param bizType business scene
     * @return latest unused code record
     */
    UserSmsCode selectLatestUnused(@Param("phone") String phone, @Param("bizType") String bizType);

    /**
     * Counts verification codes created since the specified time.
     *
     * @param createdSince lower bound for the creation time
     * @return created verification code count
     */
    long countCreatedSince(@Param("createdSince") LocalDateTime createdSince);

    /**
     * Counts verification codes consumed since the specified time.
     *
     * @param usedSince lower bound for the consume time
     * @return consumed verification code count
     */
    long countUsedSince(@Param("usedSince") LocalDateTime usedSince);

    /**
     * Updates the status of a verification code after it is consumed or expires.
     *
     * @param id code id
     * @param status latest code status
     * @param usedAt consume time, nullable when only marking expiration
     * @return affected row count
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("usedAt") LocalDateTime usedAt);
}
