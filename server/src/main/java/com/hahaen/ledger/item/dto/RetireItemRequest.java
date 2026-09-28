package com.hahaen.ledger.item.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record RetireItemRequest(
    @NotNull LocalDate retiredOn,
    @NotNull @Min(0) @Max(99999999999L) Long resaleCents,
    @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{16,64}") String idempotencyKey
) {}
