package com.hahaen.ledger.home.vo;

import com.hahaen.ledger.transaction.vo.TransactionVO;

import java.util.List;

public record HomeSummaryVO(
        String month,
        long dailyExpenseCents,
        long expenseCents,
        long incomeCents,
        long balanceCents,
        List<TransactionVO> transactions) {
}
