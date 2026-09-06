package com.hahaen.ledger.home.controller;
import com.hahaen.ledger.common.response.ApiResponse;
import com.hahaen.ledger.transaction.service.TransactionService;
import com.hahaen.ledger.home.vo.HomeSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.YearMonth;
@RestController @RequestMapping("/api/app/home") @RequiredArgsConstructor
public class HomeController { private final TransactionService service; @GetMapping("/summary") public ApiResponse<HomeSummaryVO> summary(@RequestParam @DateTimeFormat(pattern="yyyy-MM") String month){var m=YearMonth.parse(month);var h=service.home(m);return ApiResponse.ok(new HomeSummaryVO(month,h.daily(),h.expense(),h.income(),h.income()-h.expense(),h.transactions()));} }
