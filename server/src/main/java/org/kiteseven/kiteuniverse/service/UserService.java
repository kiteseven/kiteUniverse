package org.kiteseven.kiteuniverse.service;

import org.kiteseven.kiteuniverse.pojo.dto.user.UserInfoUpdateDTO;
import org.kiteseven.kiteuniverse.pojo.dto.user.UserRegisterDTO;
import org.kiteseven.kiteuniverse.pojo.vo.user.UserDetailVO;

/**
 * User service interface used by registration and profile flows.
 */
public interface UserService {

    /**
     * Registers a user through the legacy username flow.
     *
     * @param userRegisterDTO request body
     * @return created user id
     */
    Long registerUser(UserRegisterDTO userRegisterDTO);

    /**
     * Loads the complete user detail record.
     *
     * @param userId user id
     * @return user detail
     */
    UserDetailVO getUserDetail(Long userId);

    /**
     * Updates the editable profile information for the specified user.
     *
     * @param userId user id
     * @param userInfoUpdateDTO request body
     */
    void updateUserInfo(Long userId, UserInfoUpdateDTO userInfoUpdateDTO);

    /**
     * Updates the avatar path stored on the user account.
     *
     * @param userId user id
     * @param avatar avatar public path
     */
    void updateUserAvatar(Long userId, String avatar);
}
