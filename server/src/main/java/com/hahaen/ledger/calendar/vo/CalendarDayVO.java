package com.hahaen.ledger.calendar.vo;

public record CalendarDayVO(
        String date,
        int day,
        boolean currentMonth,
        boolean today,
        boolean hasRecords,
        long expenseCents,
        long incomeCents,
        long balanceCents) {
}
