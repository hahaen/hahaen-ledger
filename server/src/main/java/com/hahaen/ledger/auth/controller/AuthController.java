package com.hahaen.ledger.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.hahaen.ledger.auth.dto.WechatLoginRequest;
import com.hahaen.ledger.auth.service.WechatLoginService;
import com.hahaen.ledger.auth.vo.LoginVO;
import com.hahaen.ledger.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/app/auth") @RequiredArgsConstructor
public class AuthController {
    private final WechatLoginService loginService;
    @PostMapping("/login") public ApiResponse<LoginVO> login(@Valid @RequestBody WechatLoginRequest request) { return ApiResponse.ok(loginService.login(request.code())); }
    @PostMapping("/logout") public ApiResponse<Void> logout() { StpUtil.logout(); return ApiResponse.ok(); }
}
