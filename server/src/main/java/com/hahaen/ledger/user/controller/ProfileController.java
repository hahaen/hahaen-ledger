package com.hahaen.ledger.user.controller;

import com.hahaen.ledger.common.response.ApiResponse;
import com.hahaen.ledger.user.dto.ProfilePasswordUpdateRequest;
import com.hahaen.ledger.user.dto.ProfileUpdateRequest;
import com.hahaen.ledger.user.service.ProfileService;
import com.hahaen.ledger.user.vo.ProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PutMapping("/profile")
    public ApiResponse<ProfileVO> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.ok(profileService.updateProfile(request));
    }

    @PutMapping("/profile/password")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody ProfilePasswordUpdateRequest request) {
        profileService.updatePassword(request);
        return ApiResponse.ok();
    }
}
