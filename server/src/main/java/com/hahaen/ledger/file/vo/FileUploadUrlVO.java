package com.hahaen.ledger.file.vo;

public record FileUploadUrlVO(String fileId, String uploadUrl, int expiresInSeconds, String status) {
}
