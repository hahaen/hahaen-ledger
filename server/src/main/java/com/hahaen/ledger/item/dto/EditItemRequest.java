package com.hahaen.ledger.item.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EditItemRequest(
    @NotBlank @Size(max = 80) String name,
    @NotNull @Min(0) @Max(99999999999L) Long priceCents,
    @NotNull LocalDate purchasedOn,
    @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{16,64}") String idempotencyKey
) {}
