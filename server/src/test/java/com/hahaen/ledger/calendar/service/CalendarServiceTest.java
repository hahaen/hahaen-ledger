package com.hahaen.ledger.calendar.service;

import com.hahaen.ledger.common.security.CurrentUser;
import com.hahaen.ledger.transaction.entity.TransactionDetail;
import com.hahaen.ledger.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CalendarServiceTest {
    @Test
    void leapFebruaryProducesSixWeekGridAndMarksFebruary29() {
        TransactionService transactions = mock(TransactionService.class);
        TransactionDetail row = row(LocalDate.of(2024, 2, 29), "EXPENSE", 123L);
        when(transactions.activeInMonth(7L, YearMonth.of(2024, 2))).thenReturn(List.of(row));
        CalendarService service = new CalendarService(transactions);
        try (MockedStatic<CurrentUser> ignored = mockStatic(CurrentUser.class)) {
            ignored.when(CurrentUser::id).thenReturn(7L);
            var result = service.month(2024, 2);
            assertEquals(42, result.days().size());
            var leapDay = result.days().stream().filter(day -> day.date().equals("2024-02-29")).findFirst().orElseThrow();
            assertTrue(leapDay.currentMonth());
            assertTrue(leapDay.hasRecords());
            assertEquals(123L, leapDay.expenseCents());
        }
    }

    @Test
    void daySummaryExcludesTransferFromIncomeAndExpense() {
        TransactionService transactions = mock(TransactionService.class);
        when(transactions.activeInMonth(7L, YearMonth.of(2024, 2))).thenReturn(List.of(row(LocalDate.of(2024, 2, 1), "INCOME", 300L), row(LocalDate.of(2024, 2, 1), "TRANSFER", 800L)));
        CalendarService service = new CalendarService(transactions);
        try (MockedStatic<CurrentUser> ignored = mockStatic(CurrentUser.class)) {
            ignored.when(CurrentUser::id).thenReturn(7L);
            var result = service.day(LocalDate.of(2024, 2, 1));
            assertEquals(300L, result.incomeCents());
            assertEquals(300L, result.balanceCents());
            assertEquals(2, result.transactions().size());
        }
    }

    private static TransactionDetail row(LocalDate date, String type, long amount) {
        TransactionDetail value = new TransactionDetail(); value.setId(amount); value.setTransactionType(type); value.setAmount(amount); value.setOriginalAmount(amount); value.setHasRefund(0); value.setOccurredAt(date.atStartOfDay()); return value;
    }
}
