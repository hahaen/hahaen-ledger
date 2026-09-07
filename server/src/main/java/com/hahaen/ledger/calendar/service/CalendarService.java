package com.hahaen.ledger.calendar.service;

import com.hahaen.ledger.calendar.vo.CalendarDayDetailVO;
import com.hahaen.ledger.calendar.vo.CalendarDayVO;
import com.hahaen.ledger.calendar.vo.CalendarMonthVO;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.CurrentUser;
import com.hahaen.ledger.transaction.entity.TransactionDetail;
import com.hahaen.ledger.transaction.service.TransactionService;
import com.hahaen.ledger.transaction.vo.TransactionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CalendarService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private final TransactionService transactionService;

    public CalendarMonthVO month(int year, int month) {
        YearMonth yearMonth = parseYearMonth(year, month);
        List<TransactionDetail> transactions = transactionService.activeInMonth(CurrentUser.id(), yearMonth);
        Map<LocalDate, Totals> totals = totals(transactions);
        LocalDate first = yearMonth.atDay(1);
        int offset = first.getDayOfWeek() == DayOfWeek.SUNDAY ? 0 : first.getDayOfWeek().getValue();
        LocalDate gridStart = first.minusDays(offset);
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        List<CalendarDayVO> days = java.util.stream.IntStream.range(0, 42).mapToObj(index -> {
            LocalDate date = gridStart.plusDays(index);
            Totals value = totals.getOrDefault(date, new Totals());
            return new CalendarDayVO(date.toString(), date.getDayOfMonth(), date.getMonthValue() == month,
                    date.equals(today), value.hasRecords, value.expense, value.income, value.income - value.expense);
        }).toList();
        return new CalendarMonthVO(yearMonth.toString(), days);
    }

    public CalendarDayDetailVO day(LocalDate date) {
        List<TransactionDetail> transactions = transactionService.activeInMonth(CurrentUser.id(), YearMonth.from(date)).stream()
                .filter(row -> date.equals(row.getOccurredAt().toLocalDate())).toList();
        long expense = sum(transactions, "EXPENSE");
        long income = sum(transactions, "INCOME");
        return new CalendarDayDetailVO(date.toString(), expense, income, income - expense,
                transactions.stream().map(TransactionService::toVO).toList());
    }

    private static Map<LocalDate, Totals> totals(List<TransactionDetail> rows) {
        Map<LocalDate, Totals> map = new HashMap<>();
        for (TransactionDetail row : rows) {
            Totals value = map.computeIfAbsent(row.getOccurredAt().toLocalDate(), ignored -> new Totals());
            value.hasRecords = true;
            if ("EXPENSE".equals(row.getTransactionType())) value.expense += value(row.getAmount());
            if ("INCOME".equals(row.getTransactionType())) value.income += value(row.getAmount());
        }
        return map;
    }

    private static long sum(List<TransactionDetail> rows, String type) {
        return rows.stream().filter(row -> type.equals(row.getTransactionType())).mapToLong(row -> value(row.getAmount())).sum();
    }

    private static YearMonth parseYearMonth(int year, int month) {
        try { return YearMonth.of(year, month); }
        catch (DateTimeException ex) { throw new BusinessException("MONTH_INVALID", "年月参数不正确"); }
    }

    private static long value(Long value) { return value == null ? 0 : value; }

    private static final class Totals {
        private boolean hasRecords;
        private long expense;
        private long income;
    }
}
