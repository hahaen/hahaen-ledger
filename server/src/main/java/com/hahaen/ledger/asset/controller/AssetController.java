package com.hahaen.ledger.asset.controller;

import com.hahaen.ledger.account.vo.AssetOverviewVO;
import com.hahaen.ledger.asset.service.AssetService;
import com.hahaen.ledger.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/assets")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService assetService;

    @GetMapping("/overview")
    public ApiResponse<AssetOverviewVO> overview() {
        return ApiResponse.ok(assetService.overview());
    }
}
