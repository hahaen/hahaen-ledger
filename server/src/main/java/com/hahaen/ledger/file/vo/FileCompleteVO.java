package com.hahaen.ledger.file.vo;

public record FileCompleteVO(String fileId, String status, Long fileSize, String contentType) {
}
