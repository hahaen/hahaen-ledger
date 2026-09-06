package com.hahaen.ledger.file.vo;

public record FileCompleteVO(Long fileId, String status, Long fileSize, String contentType) {
}
