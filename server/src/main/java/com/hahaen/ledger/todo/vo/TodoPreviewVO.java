package com.hahaen.ledger.todo.vo;

import java.time.LocalDateTime;
import java.util.List;

public record TodoPreviewVO(List<LocalDateTime> times, boolean afterCompletion, boolean scheduleChanged) {}
