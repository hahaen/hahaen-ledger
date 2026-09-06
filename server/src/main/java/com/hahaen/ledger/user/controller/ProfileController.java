package com.hahaen.ledger.user.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hahaen.ledger.common.response.ApiResponse;
import com.hahaen.ledger.common.security.CurrentUser;
import com.hahaen.ledger.user.entity.AppUser;
import com.hahaen.ledger.user.mapper.AppUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/app/user") @RequiredArgsConstructor
public class ProfileController { private final AppUserMapper mapper; @GetMapping("/profile") public ApiResponse<AppUser> profile(){return ApiResponse.ok(mapper.selectById(CurrentUser.id()));} }
