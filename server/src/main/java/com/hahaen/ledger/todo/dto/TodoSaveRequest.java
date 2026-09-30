package com.hahaen.ledger.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record TodoSaveRequest(
        @NotBlank @Size(max = 100) String title,
        @Size(max = 500) String note,
        @NotBlank String recurrence,
        Integer monthInterval,
        @NotNull LocalDateTime dueAt,
        boolean remind,
        @NotBlank String idempotencyKey) {}
