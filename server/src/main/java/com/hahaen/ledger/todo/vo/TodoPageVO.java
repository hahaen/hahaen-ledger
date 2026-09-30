package com.hahaen.ledger.todo.vo;
import java.util.List;
public record TodoPageVO(long pendingCount, long completedCount, List<TodoItemVO> items, boolean hasMore) {}
