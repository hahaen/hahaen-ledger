package com.hahaen.ledger.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NotificationConfigRequest(
        @Size(max = 1024, message = "通知Key参数过长") String encryptedKey,
        boolean remove,
        @NotBlank(message = "幂等键不能为空") @Size(max = 64, message = "幂等键过长") String idempotencyKey
) {
}
