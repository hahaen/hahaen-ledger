package com.hahaen.ledger.todo.vo;
import java.time.LocalDateTime;
public record TodoItemVO(String id, String ruleId, String title, String note, String recurrence,
                         int monthInterval, LocalDateTime dueAt, LocalDateTime anchorAt, boolean remind, String status,
                         LocalDateTime completedAt, String repeatMode, String repeatUnit, Integer repeatInterval,
                         String weekDays, String monthDays, boolean lastDay, String yearDays, String fixedDates) {}
