package com.hahaen.ledger.item.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record ItemRequest(
    @NotBlank @Size(max = 80) String name,
    @NotNull @Min(0) @Max(99999999999L) Long priceCents,
    @NotNull LocalDate purchasedOn,
    @NotNull Boolean serving,
    LocalDate retiredOn,
    @Min(0) @Max(99999999999L) Long resaleCents,
    @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{16,64}") String idempotencyKey
) {}
