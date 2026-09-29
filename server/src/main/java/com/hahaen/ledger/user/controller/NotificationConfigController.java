package com.hahaen.ledger.user.controller;

import com.hahaen.ledger.common.response.ApiResponse;
import com.hahaen.ledger.user.dto.NotificationConfigRequest;
import com.hahaen.ledger.user.service.NotificationConfigService;
import com.hahaen.ledger.user.vo.NotificationConfigVO;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/app/user/notification-configs")
@RequiredArgsConstructor
public class NotificationConfigController {
    private final NotificationConfigService service;

    @GetMapping
    public ApiResponse<List<NotificationConfigVO>> list(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return ApiResponse.ok(service.list());
    }

    @PutMapping("/{type}")
    public ApiResponse<NotificationConfigVO> save(@PathVariable String type, @Valid @RequestBody NotificationConfigRequest request) {
        return ApiResponse.ok(service.save(type, request));
    }
}
