package com.hahaen.ledger.file.vo;

public record FileUploadUrlVO(Long fileId, String uploadUrl, int expiresInSeconds, String status) {
}
