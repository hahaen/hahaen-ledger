package com.hahaen.ledger.transaction.controller;
import com.hahaen.ledger.common.response.ApiResponse;
import com.hahaen.ledger.transaction.dto.RepaymentRequest;
import com.hahaen.ledger.transaction.entity.LedgerTransaction;
import com.hahaen.ledger.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/app/accounts") @RequiredArgsConstructor
public class RepaymentController { private final TransactionService service; @PostMapping("/{creditId}/repayments") public ApiResponse<LedgerTransaction> repay(@PathVariable long creditId,@Valid @RequestBody RepaymentRequest r){return ApiResponse.ok(service.repay(creditId,r));} }
