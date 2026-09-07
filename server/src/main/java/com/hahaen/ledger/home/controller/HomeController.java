package com.hahaen.ledger.home.controller;

import com.hahaen.ledger.common.response.ApiResponse;
import com.hahaen.ledger.home.service.HomeService;
import com.hahaen.ledger.home.vo.HomeSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/home")
@RequiredArgsConstructor
public class HomeController {
    private final HomeService homeService;

    @GetMapping("/summary")
    public ApiResponse<HomeSummaryVO> summary(@RequestParam(required = false) String month) {
        return ApiResponse.ok(homeService.summary(month));
    }
}
