package com.hahaen.ledger.todo.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/** 只含计划字段；预览无写入、无幂等或通知配置要求。 */
public record TodoPlanRequest(@NotBlank String recurrence, Integer monthInterval, @NotNull LocalDateTime dueAt,
        String repeatMode, String repeatUnit, Integer repeatInterval,
        @Size(max = 20) String weekDays, @Size(max = 100) String monthDays, boolean lastDay,
        @Size(max = 2200) String yearDays, @Size(max = 1100) String fixedDates) {}
