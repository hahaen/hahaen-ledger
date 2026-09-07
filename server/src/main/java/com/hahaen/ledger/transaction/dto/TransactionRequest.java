package com.hahaen.ledger.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransactionRequest(
        @NotBlank(message = "账单类型不能为空") String type,
        @NotNull(message = "金额不能为空") Long amountCents,
        Long accountId,
        Long fromAccountId,
        Long toAccountId,
        @NotBlank(message = "记账时间不能为空") String occurredAt,
        String note,
        String idempotencyKey) {
}
