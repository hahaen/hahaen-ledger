package com.hahaen.ledger.user.controller;

import com.hahaen.ledger.common.response.ApiResponse;
import com.hahaen.ledger.user.service.ProfileService;
import com.hahaen.ledger.user.vo.ProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/user")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping("/profile")
    public ApiResponse<ProfileVO> profile() {
        return ApiResponse.ok(profileService.currentProfile());
    }
}
