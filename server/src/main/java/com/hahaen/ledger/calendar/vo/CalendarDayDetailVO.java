package com.hahaen.ledger.calendar.vo;

import com.hahaen.ledger.transaction.vo.TransactionVO;

import java.util.List;

public record CalendarDayDetailVO(
        String date,
        long expenseCents,
        long incomeCents,
        long balanceCents,
        List<TransactionVO> transactions) {
}
