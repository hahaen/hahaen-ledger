package com.hahaen.ledger.asset.controller;
import com.hahaen.ledger.asset.service.AssetService;
import com.hahaen.ledger.asset.vo.AssetSummaryVO;
import com.hahaen.ledger.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/app/assets") @RequiredArgsConstructor
public class AssetController { private final AssetService service; @GetMapping("/summary") public ApiResponse<AssetSummaryVO> summary(){return ApiResponse.ok(service.summary());} }
