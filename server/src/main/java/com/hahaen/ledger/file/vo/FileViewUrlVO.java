package com.hahaen.ledger.file.vo;

/**
 * viewUrl is a short-lived signed URL. objectKey is the stable storage locator and is never a
 * public MinIO URL.
 */
public record FileViewUrlVO(String fileId, String viewUrl, int expiresInSeconds, String objectKey) {
}
