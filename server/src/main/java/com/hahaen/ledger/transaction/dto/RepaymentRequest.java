package com.hahaen.ledger.transaction.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
public record RepaymentRequest(@NotNull Long fundAccountId, @NotNull Long amountCents, @Size(max=80) String idempotencyKey) {}
