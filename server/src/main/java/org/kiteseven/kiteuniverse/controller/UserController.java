package org.kiteseven.kiteuniverse.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.kiteseven.kiteuniverse.common.result.Result;
import org.kiteseven.kiteuniverse.pojo.dto.user.UserInfoUpdateDTO;
import org.kiteseven.kiteuniverse.pojo.dto.user.UserRegisterDTO;
import org.kiteseven.kiteuniverse.pojo.vo.user.UserDetailVO;
import org.kiteseven.kiteuniverse.service.FileStorageService;
import org.kiteseven.kiteuniverse.service.UserService;
import org.kiteseven.kiteuniverse.support.auth.UserTokenService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Exposes user profile endpoints for both public detail views and the personal center.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserTokenService userTokenService;
    private final FileStorageService fileStorageService;

    public UserController(UserService userService,
                          UserTokenService userTokenService,
                          FileStorageService fileStorageService) {
        this.userService = userService;
        this.userTokenService = userTokenService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Registers a user through the legacy username and password flow.
     *
     * @param userRegisterDTO request body
     * @return created user id
     */
    @PostMapping("/register")
    public Result<Long> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        return Result.success(userService.registerUser(userRegisterDTO));
    }

    /**
     * Loads a public user detail record by id.
     *
     * @param userId target user id
     * @return user detail
     */
    @GetMapping("/{userId}")
    public Result<UserDetailVO> getDetail(@PathVariable Long userId) {
        return Result.success(userService.getUserDetail(userId));
    }

    /**
     * Updates a user profile by id. This endpoint is kept for compatibility.
     *
     * @param userId target user id
     * @param userInfoUpdateDTO request body
     * @return empty success response
     */
    @PutMapping("/{userId}/info")
    public Result<Void> updateInfo(@PathVariable Long userId, @RequestBody UserInfoUpdateDTO userInfoUpdateDTO) {
        userService.updateUserInfo(userId, userInfoUpdateDTO);
        return Result.success();
    }

    /**
     * Loads the full personal-center profile for the current logged-in user.
     *
     * @param request current HTTP request
     * @return current user detail
     */
    @GetMapping("/me")
    public Result<UserDetailVO> getCurrentUserDetail(HttpServletRequest request) {
        Long userId = resolveCurrentUserId(request);
        return Result.success(userService.getUserDetail(userId));
    }

    /**
     * Updates the current user's personal profile and returns the latest data.
     *
     * @param request current HTTP request
     * @param userInfoUpdateDTO request body
     * @return updated user detail
     */
    @PutMapping("/me/info")
    public Result<UserDetailVO> updateCurrentUserInfo(HttpServletRequest request,
                                                      @RequestBody UserInfoUpdateDTO userInfoUpdateDTO) {
        Long userId = resolveCurrentUserId(request);
        userService.updateUserInfo(userId, userInfoUpdateDTO);
        return Result.success(userService.getUserDetail(userId));
    }

    /**
     * Uploads a new avatar for the current user and returns the refreshed profile.
     *
     * @param request current HTTP request
     * @param file avatar image
     * @return updated user detail
     */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<UserDetailVO> uploadCurrentUserAvatar(HttpServletRequest request,
                                                        @RequestParam("file") MultipartFile file) {
        Long userId = resolveCurrentUserId(request);
        String avatarPath = fileStorageService.storeAvatar(file, userId);
        userService.updateUserAvatar(userId, avatarPath);
        return Result.success(userService.getUserDetail(userId));
    }

    /**
     * Resolves the authenticated user id from the current request token.
     *
     * @param request current HTTP request
     * @return authenticated user id
     */
    private Long resolveCurrentUserId(HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        return userTokenService.parseUserId(token);
    }
}
