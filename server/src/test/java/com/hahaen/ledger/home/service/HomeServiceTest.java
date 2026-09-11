package com.hahaen.ledger.home.service;

import com.hahaen.ledger.common.security.CurrentUser;
import com.hahaen.ledger.home.vo.HomeSummaryVO;
import com.hahaen.ledger.home.vo.HomeRecentTransactionsVO;
import com.hahaen.ledger.transaction.entity.TransactionDetail;
import com.hahaen.ledger.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class HomeServiceTest {
    @Test
    void summaryCountsOnlyExpenseAndIncomeAndKeepsTransferOutOfBalance() {
        TransactionService transactions = mock(TransactionService.class);
        when(transactions.activeInMonth(7L, YearMonth.of(2026, 9))).thenReturn(List.of(row("EXPENSE", 300L), row("INCOME", 1_000L), row("TRANSFER", 500L)));
        HomeService service = new HomeService(transactions);
        try (MockedStatic<CurrentUser> ignored = mockStatic(CurrentUser.class)) {
            ignored.when(CurrentUser::id).thenReturn(7L);
            HomeSummaryVO result = service.summary("2026-09");
            assertEquals(300L, result.expenseCents());
            assertEquals(1_000L, result.incomeCents());
            assertEquals(700L, result.balanceCents());
        }
    }

    @Test
    void dailyAverageUsesPastDaysForCurrentMonthAndFullDaysForCompletedMonth() {
        assertEquals(7, HomeService.dailyAverageDays(YearMonth.of(2026, 9), LocalDate.of(2026, 9, 7)));
        assertEquals(31, HomeService.dailyAverageDays(YearMonth.of(2026, 8), LocalDate.of(2026, 9, 7)));
        assertEquals(0, HomeService.dailyAverageDays(YearMonth.of(2026, 10), LocalDate.of(2026, 9, 7)));
    }

    @Test
    void recentTransactionsLoadsTwoMonthsBeforeExclusiveCursorAndReportsMore() {
        TransactionService transactions = mock(TransactionService.class);
        LocalDateTime start = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 1, 0, 0);
        when(transactions.activeInPeriod(7L, start, end)).thenReturn(List.of());
        when(transactions.hasActiveBefore(7L, start)).thenReturn(true);
        HomeService service = new HomeService(transactions);
        try (MockedStatic<CurrentUser> ignored = mockStatic(CurrentUser.class)) {
            ignored.when(CurrentUser::id).thenReturn(7L);
            HomeRecentTransactionsVO result = service.recentTransactions("2026-09");
            assertEquals("2026-07", result.startMonth());
            assertEquals("2026-08", result.endMonth());
            assertEquals(true, result.hasMore());
        }
        verify(transactions).activeInPeriod(7L, start, end);
        verify(transactions).hasActiveBefore(7L, start);
    }

    private static TransactionDetail row(String type, long amount) {
        TransactionDetail value = new TransactionDetail(); value.setId(amount); value.setTransactionType(type); value.setAmount(amount); value.setOriginalAmount(amount); value.setHasRefund(0); value.setOccurredAt(LocalDate.of(2026, 9, 7).atStartOfDay()); return value;
    }
}
