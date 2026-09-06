package com.hahaen.ledger.file.controller;

import com.hahaen.ledger.common.response.ApiResponse;
import com.hahaen.ledger.file.dto.FileUploadUrlRequest;
import com.hahaen.ledger.file.service.AppFileService;
import com.hahaen.ledger.file.vo.FileCompleteVO;
import com.hahaen.ledger.file.vo.FileUploadUrlVO;
import com.hahaen.ledger.file.vo.FileViewUrlVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/files")
@RequiredArgsConstructor
public class FileController {
    private final AppFileService fileService;

    @PostMapping("/upload-url")
    public ApiResponse<FileUploadUrlVO> uploadUrl(@Valid @RequestBody FileUploadUrlRequest request) {
        return ApiResponse.ok(fileService.createUploadUrl(request));
    }

    @PostMapping("/{fileId}/complete")
    public ApiResponse<FileCompleteVO> complete(@PathVariable long fileId) {
        return ApiResponse.ok(fileService.complete(fileId));
    }

    @GetMapping("/{fileId}/view-url")
    public ApiResponse<FileViewUrlVO> viewUrl(@PathVariable long fileId) {
        return ApiResponse.ok(fileService.viewUrl(fileId));
    }

    @GetMapping("/avatar/view-url")
    public ApiResponse<FileViewUrlVO> currentAvatarViewUrl() {
        return ApiResponse.ok(fileService.currentAvatarViewUrl());
    }

    @DeleteMapping("/{fileId}")
    public ApiResponse<Void> delete(@PathVariable long fileId) {
        fileService.delete(fileId);
        return ApiResponse.ok();
    }
}
