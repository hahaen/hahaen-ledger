package com.hahaen.ledger.transaction.vo;

import java.util.List;

public record TransactionPageVO(
        List<TransactionVO> items,
        long total,
        int page,
        int pageSize) {
}
