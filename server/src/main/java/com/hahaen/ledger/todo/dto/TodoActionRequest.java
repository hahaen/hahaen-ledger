package com.hahaen.ledger.todo.dto;
import jakarta.validation.constraints.NotBlank;
public record TodoActionRequest(@NotBlank String idempotencyKey) {}
