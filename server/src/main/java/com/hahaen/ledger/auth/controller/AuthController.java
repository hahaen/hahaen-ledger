package com.hahaen.ledger.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.hahaen.ledger.auth.dto.H5AuthRequest;
import com.hahaen.ledger.auth.service.CaptchaService;
import com.hahaen.ledger.auth.service.H5AuthService;
import com.hahaen.ledger.auth.service.PasswordCryptoService;
import com.hahaen.ledger.auth.vo.CaptchaVO;
import com.hahaen.ledger.auth.vo.LoginVO;
import com.hahaen.ledger.auth.vo.PasswordKeyVO;
import com.hahaen.ledger.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/auth")
@RequiredArgsConstructor
public class AuthController {
    private final H5AuthService authService;
    private final CaptchaService captchaService;
    private final PasswordCryptoService passwordCryptoService;

    @GetMapping("/password-key")
    public ApiResponse<PasswordKeyVO> passwordKey() {
        return ApiResponse.ok(new PasswordKeyVO(passwordCryptoService.publicKey()));
    }

    @GetMapping("/captcha")
    public ApiResponse<CaptchaVO> captcha() {
        var captcha = captchaService.create();
        return ApiResponse.ok(new CaptchaVO(captcha.id(), captcha.image(), captcha.expiresInSeconds()));
    }

    @PostMapping("/h5/register")
    public ApiResponse<Void> register(@Valid @RequestBody H5AuthRequest request, HttpServletRequest servletRequest) {
        authService.register(request, clientIp(servletRequest), servletRequest.getHeader("User-Agent"));
        return ApiResponse.ok();
    }

    @PostMapping("/h5/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody H5AuthRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.ok(authService.login(request, clientIp(servletRequest), servletRequest.getHeader("User-Agent")));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        StpUtil.logout();
        return ApiResponse.ok();
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",", 2)[0].trim();
        return request.getRemoteAddr();
    }
}
