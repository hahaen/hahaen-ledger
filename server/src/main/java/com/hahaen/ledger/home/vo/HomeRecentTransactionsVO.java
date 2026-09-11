package com.hahaen.ledger.home.vo;

import com.hahaen.ledger.transaction.vo.TransactionVO;

import java.util.List;

public record HomeRecentTransactionsVO(
        String startMonth,
        String endMonth,
        List<TransactionVO> transactions,
        boolean hasMore) {
}
