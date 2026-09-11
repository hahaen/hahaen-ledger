package com.hahaen.ledger.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FileUploadUrlRequest(
        @NotBlank @Size(max = 32) String businessType,
        @NotBlank @Size(max = 255) String originalName,
        @NotBlank @Size(max = 128) String contentType,
        @NotNull Long fileSize,
        @NotBlank @Pattern(regexp = "[0-9a-fA-F]{64}") String fileHash,
        @NotBlank @Size(max = 80) String idempotencyKey
) {
}
