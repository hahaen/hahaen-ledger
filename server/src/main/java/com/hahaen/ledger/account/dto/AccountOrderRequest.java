package com.hahaen.ledger.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AccountOrderRequest(
        @Positive(message = "目标账户无效") long targetAccountId,
        @Positive(message = "账户顺序无效") int expectedSortOrder,
        @Positive(message = "目标账户顺序无效") int targetExpectedSortOrder,
        @NotBlank(message = "幂等键不能为空") @Size(max = 80, message = "幂等键最多80个字符") String idempotencyKey) {
}
