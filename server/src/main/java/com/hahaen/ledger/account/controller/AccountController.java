package com.hahaen.ledger.account.controller;

import com.hahaen.ledger.account.dto.AccountRequest;
import com.hahaen.ledger.account.entity.LedgerAccount;
import com.hahaen.ledger.account.service.AccountService;
import com.hahaen.ledger.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/app/accounts") @RequiredArgsConstructor
public class AccountController {
    private final AccountService service;
    @GetMapping public ApiResponse<List<LedgerAccount>> list() { return ApiResponse.ok(service.list()); }
    @GetMapping("/{id}") public ApiResponse<LedgerAccount> get(@PathVariable long id) { return ApiResponse.ok(service.require(id)); }
    @PostMapping public ApiResponse<LedgerAccount> create(@Valid @RequestBody AccountRequest request) { return ApiResponse.ok(service.create(request)); }
    @PutMapping("/{id}") public ApiResponse<LedgerAccount> update(@PathVariable long id, @Valid @RequestBody AccountRequest request) { return ApiResponse.ok(service.update(id, request)); }
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable long id) { service.disable(id); return ApiResponse.ok(); }
}
