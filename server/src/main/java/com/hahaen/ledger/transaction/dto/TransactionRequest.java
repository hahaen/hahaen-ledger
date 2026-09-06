package com.hahaen.ledger.transaction.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
public record TransactionRequest(@NotBlank @Size(max=16) String type, @NotNull @Positive Long amountCents, Long accountId, Long fromAccountId, Long toAccountId, @NotNull LocalDateTime occurredAt, @Size(max=100) String note, @Size(max=80) String idempotencyKey) {}
