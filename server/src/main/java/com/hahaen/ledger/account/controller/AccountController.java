package com.hahaen.ledger.account.controller;

import com.hahaen.ledger.account.dto.AccountRequest;
import com.hahaen.ledger.account.service.AccountService;
import com.hahaen.ledger.account.vo.AccountVO;
import com.hahaen.ledger.common.response.ApiResponse;
import com.hahaen.ledger.transaction.dto.RepaymentRequest;
import com.hahaen.ledger.transaction.service.TransactionService;
import com.hahaen.ledger.transaction.vo.TransactionPageVO;
import com.hahaen.ledger.transaction.vo.TransactionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/app/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping
    public ApiResponse<List<AccountVO>> list() {
        return ApiResponse.ok(accountService.list());
    }

    @PostMapping
    public ApiResponse<AccountVO> create(@Valid @RequestBody AccountRequest request) {
        return ApiResponse.ok(accountService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<AccountVO> detail(@PathVariable long id) {
        return ApiResponse.ok(accountService.get(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<AccountVO> update(@PathVariable long id, @Valid @RequestBody AccountRequest request) {
        return ApiResponse.ok(accountService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable long id) {
        accountService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/transactions")
    public ApiResponse<TransactionPageVO> transactions(@PathVariable long id,
                                                       @RequestParam(required = false) String type,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "50") int pageSize) {
        return ApiResponse.ok(transactionService.list(null, null, id, type, page, pageSize));
    }

    @PostMapping("/{id}/repayments")
    public ApiResponse<TransactionVO> repayment(@PathVariable long id, @Valid @RequestBody RepaymentRequest request) {
        return ApiResponse.ok(transactionService.repay(id, request));
    }
}
