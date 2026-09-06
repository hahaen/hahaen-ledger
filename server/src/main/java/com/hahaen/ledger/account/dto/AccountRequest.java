package com.hahaen.ledger.account.dto;
import jakarta.validation.constraints.*;
public record AccountRequest(@NotBlank @Size(max=20) String name, @NotBlank String kind, @NotNull @PositiveOrZero Long balanceCents, @NotNull @PositiveOrZero Long creditLimitCents, boolean includedInNetAsset) {}
