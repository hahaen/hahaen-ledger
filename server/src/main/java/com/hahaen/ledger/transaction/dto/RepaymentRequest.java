package com.hahaen.ledger.transaction.dto;

import jakarta.validation.constraints.NotNull;

public record RepaymentRequest(
        @NotNull(message = "资金账户不能为空") Long fundAccountId,
        @NotNull(message = "还款金额不能为空") Long amountCents,
        String idempotencyKey) {
}
