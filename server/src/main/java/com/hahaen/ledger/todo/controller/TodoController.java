package com.hahaen.ledger.todo.controller;

import com.hahaen.ledger.common.response.ApiResponse;
import com.hahaen.ledger.todo.dto.TodoActionRequest;
import com.hahaen.ledger.todo.dto.TodoSaveRequest;
import com.hahaen.ledger.todo.service.TodoService;
import com.hahaen.ledger.todo.vo.TodoPageVO;
import com.hahaen.ledger.todo.vo.TodoAttemptVO;
import com.hahaen.ledger.todo.vo.TodoItemVO;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/app/todos") @RequiredArgsConstructor
public class TodoController {
    private final TodoService service;
    @GetMapping public ApiResponse<TodoPageVO> list(@RequestParam(defaultValue="PENDING") String status,
            @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int pageSize) {
        return ApiResponse.ok(service.list(status, page, pageSize));
    }
    @GetMapping("/{id}") public ApiResponse<TodoItemVO> detail(@PathVariable long id) {
        return ApiResponse.ok(service.detail(id));
    }
    @GetMapping("/{id}/attempts") public ApiResponse<List<TodoAttemptVO>> attempts(@PathVariable long id) {
        return ApiResponse.ok(service.attempts(id));
    }
    @PostMapping public ApiResponse<String> create(@Valid @RequestBody TodoSaveRequest request) {
        return ApiResponse.ok(service.create(request));
    }
    @PutMapping("/{id}") public ApiResponse<Void> edit(@PathVariable long id, @Valid @RequestBody TodoSaveRequest request) {
        service.edit(id, request); return ApiResponse.ok(null);
    }
    @PostMapping("/{id}/complete") public ApiResponse<Void> complete(@PathVariable long id, @Valid @RequestBody TodoActionRequest request) {
        service.complete(id, request.idempotencyKey()); return ApiResponse.ok(null);
    }
    @PostMapping("/{id}/delete") public ApiResponse<Void> delete(@PathVariable long id, @Valid @RequestBody TodoActionRequest request) {
        service.delete(id, request.idempotencyKey()); return ApiResponse.ok(null);
    }
}
