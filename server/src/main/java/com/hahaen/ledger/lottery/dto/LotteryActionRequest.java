package com.hahaen.ledger.lottery.dto;

import jakarta.validation.constraints.*;
public record LotteryActionRequest(
        @Pattern(regexp="ACTIVE|PAUSED|ENDED") String status,
        @NotBlank @Pattern(regexp="[A-Za-z0-9_-]{8,64}") String idempotencyKey
) {}
