package org.kiteseven.kiteuniverse.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.kiteseven.kiteuniverse.common.result.Result;
import org.kiteseven.kiteuniverse.pojo.dto.auth.PhoneLoginDTO;
import org.kiteseven.kiteuniverse.pojo.dto.auth.PhoneRegisterDTO;
import org.kiteseven.kiteuniverse.pojo.dto.auth.SmsCodeSendDTO;
import org.kiteseven.kiteuniverse.pojo.vo.auth.AuthResultVO;
import org.kiteseven.kiteuniverse.pojo.vo.auth.AuthUserVO;
import org.kiteseven.kiteuniverse.pojo.vo.auth.SmsCodeSendVO;
import org.kiteseven.kiteuniverse.service.AuthService;
import org.kiteseven.kiteuniverse.support.auth.UserTokenService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints used by the frontend login modal.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
    };

    private final AuthService authService;
    private final UserTokenService userTokenService;

    public AuthController(AuthService authService, UserTokenService userTokenService) {
        this.authService = authService;
        this.userTokenService = userTokenService;
    }

    /**
     * Creates a phone verification code for the requested business scene.
     *
     * @param smsCodeSendDTO request body
     * @param request current HTTP request
     * @return code response for frontend integration
     */
    @PostMapping("/sms-code")
    public Result<SmsCodeSendVO> sendSmsCode(@RequestBody SmsCodeSendDTO smsCodeSendDTO,
                                             HttpServletRequest request) {
        return Result.success(authService.sendSmsCode(smsCodeSendDTO, resolveClientIp(request)));
    }

    /**
     * Registers a user with a phone number and signs the user in immediately.
     *
     * @param phoneRegisterDTO request body
     * @return authenticated session
     */
    @PostMapping("/phone/register")
    public Result<AuthResultVO> registerByPhone(@RequestBody PhoneRegisterDTO phoneRegisterDTO) {
        return Result.success(authService.registerByPhone(phoneRegisterDTO));
    }

    /**
     * Logs in an existing user with a phone number and verification code.
     *
     * @param phoneLoginDTO request body
     * @return authenticated session
     */
    @PostMapping("/phone/login")
    public Result<AuthResultVO> loginByPhone(@RequestBody PhoneLoginDTO phoneLoginDTO) {
        return Result.success(authService.loginByPhone(phoneLoginDTO));
    }

    /**
     * Loads the current authenticated user based on the client token.
     *
     * @param request current HTTP request
     * @return current user summary
     */
    @GetMapping("/me")
    public Result<AuthUserVO> getCurrentUser(HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        return Result.success(authService.getCurrentUser(token));
    }

    /**
     * Handles frontend logout requests by removing the current Redis-backed session.
     *
     * @param request current HTTP request
     * @return empty success response
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (StringUtils.hasText(token)) {
            userTokenService.revokeToken(token);
        }
        return Result.success();
    }

    /**
     * Resolves the current client IP address, preferring reverse-proxy headers when they exist.
     *
     * @param request current HTTP request
     * @return normalized client IP
     */
    private String resolveClientIp(HttpServletRequest request) {
        for (String headerName : IP_HEADER_CANDIDATES) {
            String headerValue = request.getHeader(headerName);
            if (!StringUtils.hasText(headerValue)) {
                continue;
            }

            String candidateIp = headerValue.split(",")[0].trim();
            if (StringUtils.hasText(candidateIp) && !"unknown".equalsIgnoreCase(candidateIp)) {
                return normalizeIp(candidateIp);
            }
        }
        return normalizeIp(request.getRemoteAddr());
    }

    /**
     * Normalizes loopback addresses so local testing always uses the same Redis key.
     *
     * @param ip raw client IP
     * @return normalized IP
     */
    private String normalizeIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return "unknown";
        }
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip.trim();
    }
}
