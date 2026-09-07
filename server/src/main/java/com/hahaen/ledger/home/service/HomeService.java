package com.hahaen.ledger.home.service;

import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.CurrentUser;
import com.hahaen.ledger.home.vo.HomeSummaryVO;
import com.hahaen.ledger.transaction.entity.TransactionDetail;
import com.hahaen.ledger.transaction.service.TransactionService;
import com.hahaen.ledger.transaction.vo.TransactionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private final TransactionService transactionService;

    public HomeSummaryVO summary(String requestedMonth) {
        YearMonth month = parseMonth(requestedMonth);
        long userId = CurrentUser.id();
        List<TransactionDetail> transactions = transactionService.activeInMonth(userId, month);
        long expense = sum(transactions, "EXPENSE", false);
        long income = sum(transactions, "INCOME", false);
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        long dailyExpense = sum(transactions, "EXPENSE", true, today);
        int denominator = dailyAverageDays(month, today);
        return new HomeSummaryVO(month.toString(), denominator == 0 ? 0 : dailyExpense / denominator,
                expense, income, income - expense, transactions.stream().map(TransactionService::toVO).toList());
    }

    static int dailyAverageDays(YearMonth month, LocalDate today) {
        if (month.isAfter(YearMonth.from(today))) return 0;
        return month.equals(YearMonth.from(today)) ? today.getDayOfMonth() : month.lengthOfMonth();
    }

    private static long sum(List<TransactionDetail> rows, String type, boolean beforeToday) {
        return sum(rows, type, beforeToday, LocalDate.now(BUSINESS_ZONE));
    }

    private static long sum(List<TransactionDetail> rows, String type, boolean beforeToday, LocalDate today) {
        return rows.stream().filter(row -> type.equals(row.getTransactionType()))
                .filter(row -> !beforeToday || !row.getOccurredAt().toLocalDate().isAfter(today))
                .mapToLong(row -> row.getAmount() == null ? 0 : row.getAmount()).sum();
    }

    static YearMonth parseMonth(String value) {
        if (value == null || value.isBlank()) return YearMonth.now(BUSINESS_ZONE);
        try { return YearMonth.parse(value); }
        catch (DateTimeParseException ex) { throw new BusinessException("MONTH_INVALID", "月份格式应为YYYY-MM"); }
    }
}
