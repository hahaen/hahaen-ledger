package com.hahaen.ledger.lottery.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record LotterySaveRequest(
        @NotBlank @Pattern(regexp="DLT|SSQ") String game,
        @Size(max=60) String name,
        @NotBlank @Size(max=40) String red,
        @NotBlank @Size(max=12) String blue,
        @Min(1) @Max(99) int multiple,
        boolean additional,
        @Size(max=7) String issue,
        @Min(1) @Max(20) int periods,
        @NotNull @PastOrPresent LocalDate purchasedOn,
        @Size(max=500) String note,
        boolean confirmedPurchase,
        @NotBlank @Pattern(regexp="[A-Za-z0-9_-]{8,64}") String idempotencyKey
) {}
