package org.kiteseven.kiteuniverse.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kiteseven.kiteuniverse.pojo.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data access object for user accounts.
 */
@Mapper
public interface UserMapper {

    /**
     * Finds a user by id.
     *
     * @param id user id
     * @return user account
     */
    User selectById(@Param("id") Long id);

    /**
     * Finds a user by username.
     *
     * @param username username
     * @return user account
     */
    User selectByUsername(@Param("username") String username);

    /**
     * Finds a user by email.
     *
     * @param email email address
     * @return user account
     */
    User selectByEmail(@Param("email") String email);

    /**
     * Finds a user by phone number.
     *
     * @param phone mobile phone number
     * @return user account
     */
    User selectByPhone(@Param("phone") String phone);

    /**
     * Counts all registered user accounts.
     *
     * @return registered user count
     */
    long countAll();

    /**
     * Counts users created since the specified time.
     *
     * @param createdSince lower bound for account creation time
     * @return created user count
     */
    long countCreatedSince(@Param("createdSince") LocalDateTime createdSince);

    /**
     * Counts users who logged in since the specified time.
     *
     * @param lastLoginSince lower bound for the last login time
     * @return active user count
     */
    long countLastLoginSince(@Param("lastLoginSince") LocalDateTime lastLoginSince);

    /**
     * Returns all user accounts (used for broadcasting announcements).
     *
     * @return list of all users
     */
    List<User> selectAll();

    /**
     * Returns all user ids for Bloom filter warm-up.
     *
     * @return all user ids
     */
    List<Long> selectAllIds();

    /**
     * Inserts a user account.
     *
     * @param user user entity
     * @return affected row count
     */
    int insert(User user);

    /**
     * Updates visible profile fields on the account table.
     *
     * @param user user entity
     * @return affected row count
     */
    int updateProfileById(User user);

    /**
     * Updates the latest login time after a successful login.
     *
     * @param id user id
     * @param lastLoginTime latest login time
     * @return affected row count
     */
    int updateLastLoginTimeById(@Param("id") Long id, @Param("lastLoginTime") LocalDateTime lastLoginTime);
}
