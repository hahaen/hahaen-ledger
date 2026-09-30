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
        @NotBlank String idempotencyKey,
        String repeatMode, String repeatUnit, Integer repeatInterval,
        @Size(max = 20) String weekDays, @Size(max = 100) String monthDays, boolean lastDay,
        @Size(max = 2200) String yearDays, @Size(max = 1100) String fixedDates) {
    public TodoSaveRequest(String title, String note, String recurrence, Integer monthInterval,
                           LocalDateTime dueAt, boolean remind, String idempotencyKey) {
        this(title, note, recurrence, monthInterval, dueAt, remind, idempotencyKey,
                null, null, null, null, null, false, null, null);
    }
}
