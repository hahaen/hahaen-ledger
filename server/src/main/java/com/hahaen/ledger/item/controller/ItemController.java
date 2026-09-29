package com.hahaen.ledger.item.controller;
import com.hahaen.ledger.common.response.ApiResponse;
import com.hahaen.ledger.item.dto.*;
import com.hahaen.ledger.item.service.ItemService;
import com.hahaen.ledger.item.vo.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/app/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService service;
    @GetMapping
    public ApiResponse<ItemOverviewVO> list(@RequestParam(defaultValue = "ACTIVE") String status,
        @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(service.overview(status, page, pageSize));
    }
    @GetMapping("/{id}")
    public ApiResponse<ItemDetailVO> detail(@PathVariable long id) { return ApiResponse.ok(service.detail(id)); }
    @PostMapping
    public ApiResponse<ItemVO> create(@Valid @RequestBody ItemRequest request) { return ApiResponse.ok(service.create(request)); }
    @PostMapping("/{id}/retire")
    public ApiResponse<ItemVO> retire(@PathVariable long id, @Valid @RequestBody RetireItemRequest request) { return ApiResponse.ok(service.retire(id, request)); }
    @PostMapping("/{id}/reactivate")
    public ApiResponse<ItemVO> reactivate(@PathVariable long id, @Valid @RequestBody ReactivateItemRequest request) { return ApiResponse.ok(service.reactivate(id, request)); }
    @PutMapping("/{id}")
    public ApiResponse<ItemVO> edit(@PathVariable long id, @Valid @RequestBody EditItemRequest request) { return ApiResponse.ok(service.edit(id, request)); }
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable long id, @RequestParam String idempotencyKey) {
        service.delete(id, idempotencyKey); return ApiResponse.ok();
    }
}
