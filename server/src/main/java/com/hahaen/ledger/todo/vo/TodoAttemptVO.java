package com.hahaen.ledger.todo.vo;
import java.time.LocalDateTime;
public record TodoAttemptVO(String channel, String messageTitle, String messageBody, String result, LocalDateTime attemptedAt) {}
