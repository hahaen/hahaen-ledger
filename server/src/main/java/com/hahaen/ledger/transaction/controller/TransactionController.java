package com.hahaen.ledger.transaction.controller;
import com.hahaen.ledger.common.response.ApiResponse;
import com.hahaen.ledger.transaction.dto.*;
import com.hahaen.ledger.transaction.entity.*;
import com.hahaen.ledger.transaction.service.TransactionService;
import com.hahaen.ledger.transaction.vo.TransactionDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
@RestController @RequestMapping("/api/app/transactions") @RequiredArgsConstructor
public class TransactionController {
    private final TransactionService service;
    @GetMapping public ApiResponse<List<LedgerTransaction>> list(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,@RequestParam(required=false) Long accountId,@RequestParam(required=false) String type){return ApiResponse.ok(service.list(from,to,accountId,type));}
    @GetMapping("/{id}") public ApiResponse<TransactionDetailVO> get(@PathVariable long id){return ApiResponse.ok(service.detail(id));}
    @PostMapping public ApiResponse<LedgerTransaction> create(@Valid @RequestBody TransactionRequest r){return ApiResponse.ok(service.create(r));}
    @PutMapping("/{id}") public ApiResponse<LedgerTransaction> update(@PathVariable long id,@Valid @RequestBody TransactionRequest r){return ApiResponse.ok(service.update(id,r));}
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable long id){service.delete(id);return ApiResponse.ok();}
    @PostMapping("/{id}/refunds") public ApiResponse<TransactionRefund> refund(@PathVariable long id,@Valid @RequestBody RefundRequest r){return ApiResponse.ok(service.refund(id,r));}
    @DeleteMapping("/refunds/{refundId}") public ApiResponse<Void> deleteRefund(@PathVariable long refundId){service.deleteRefund(refundId);return ApiResponse.ok();}
}
