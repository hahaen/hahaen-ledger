package com.hahaen.ledger.todo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record TodoPreviewRequest(@NotNull @Valid TodoPlanRequest plan, Long occurrenceId) {}
