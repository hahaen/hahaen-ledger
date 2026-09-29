package com.hahaen.ledger.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ReactivateItemRequest(
    @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{16,64}") String idempotencyKey
) {}
