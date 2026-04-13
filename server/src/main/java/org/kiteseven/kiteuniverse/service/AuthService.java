package org.kiteseven.kiteuniverse.service;

import org.kiteseven.kiteuniverse.pojo.dto.auth.PhoneLoginDTO;
import org.kiteseven.kiteuniverse.pojo.dto.auth.PhoneRegisterDTO;
import org.kiteseven.kiteuniverse.pojo.dto.auth.SmsCodeSendDTO;
import org.kiteseven.kiteuniverse.pojo.vo.auth.AuthResultVO;
import org.kiteseven.kiteuniverse.pojo.vo.auth.AuthUserVO;
import org.kiteseven.kiteuniverse.pojo.vo.auth.SmsCodeSendVO;

/**
 * Authentication service used by the login and registration modal.
 */
public interface AuthService {

    /**
     * Sends a development-friendly verification code for the specified scene.
     *
     * @param smsCodeSendDTO request body
     * @param clientIp current client IP address
     * @return created code details
     */
    SmsCodeSendVO sendSmsCode(SmsCodeSendDTO smsCodeSendDTO, String clientIp);

    /**
     * Registers a new user with a phone number and verification code.
     *
     * @param phoneRegisterDTO request body
     * @return authenticated session data
     */
    AuthResultVO registerByPhone(PhoneRegisterDTO phoneRegisterDTO);

    /**
     * Logs in an existing user with a phone number and verification code.
     *
     * @param phoneLoginDTO request body
     * @return authenticated session data
     */
    AuthResultVO loginByPhone(PhoneLoginDTO phoneLoginDTO);

    /**
     * Loads the current user from the provided token.
     *
     * @param token raw client token
     * @return current user summary
     */
    AuthUserVO getCurrentUser(String token);
}
