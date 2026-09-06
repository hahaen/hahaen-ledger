package com.hahaen.ledger.calendar.controller;
import com.hahaen.ledger.common.response.ApiResponse;
import com.hahaen.ledger.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.YearMonth;
import java.util.Map;
@RestController @RequestMapping("/api/app/calendar") @RequiredArgsConstructor
public class CalendarController { private final TransactionService service; @GetMapping public ApiResponse<Map<String,Object>> month(@RequestParam int year,@RequestParam int month){var h=service.home(YearMonth.of(year,month)); return ApiResponse.ok(Map.of("year",year,"month",month,"transactions",h.transactions(),"expenseCents",h.expense(),"incomeCents",h.income(),"balanceCents",h.income()-h.expense()));} }
