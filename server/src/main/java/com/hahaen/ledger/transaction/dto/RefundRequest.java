package com.hahaen.ledger.transaction.dto;

import jakarta.validation.constraints.NotNull;

public record RefundRequest(
        @NotNull(message = "退款金额不能为空") Long amountCents,
        String idempotencyKey) {
}
