package com.hahaen.ledger.transaction.controller;

import com.hahaen.ledger.common.response.ApiResponse;
import com.hahaen.ledger.transaction.dto.RefundRequest;
import com.hahaen.ledger.transaction.dto.TransactionRequest;
import com.hahaen.ledger.transaction.service.TransactionService;
import com.hahaen.ledger.transaction.vo.RefundVO;
import com.hahaen.ledger.transaction.vo.TransactionDetailVO;
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

@RestController
@RequestMapping("/api/app/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public ApiResponse<TransactionPageVO> list(@RequestParam(required = false) String month,
                                               @RequestParam(required = false) String date,
                                               @RequestParam(required = false) Long accountId,
                                               @RequestParam(required = false) String type,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "50") int pageSize) {
        return ApiResponse.ok(transactionService.list(month, date, accountId, type, page, pageSize));
    }

    @PostMapping
    public ApiResponse<TransactionVO> create(@Valid @RequestBody TransactionRequest request) {
        return ApiResponse.ok(transactionService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransactionDetailVO> detail(@PathVariable long id) {
        return ApiResponse.ok(transactionService.detail(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<TransactionVO> update(@PathVariable long id, @Valid @RequestBody TransactionRequest request) {
        return ApiResponse.ok(transactionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable long id) {
        transactionService.delete(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/refunds")
    public ApiResponse<RefundVO> refund(@PathVariable long id, @Valid @RequestBody RefundRequest request) {
        return ApiResponse.ok(transactionService.createRefund(id, request));
    }

    @DeleteMapping("/refunds/{refundId}")
    public ApiResponse<Void> deleteRefund(@PathVariable long refundId) {
        transactionService.deleteRefund(refundId);
        return ApiResponse.ok();
    }
}
