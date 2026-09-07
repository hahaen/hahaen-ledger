package com.hahaen.ledger.transaction.vo;

import java.util.List;

public record TransactionDetailVO(
        TransactionVO transaction,
        long refundedCents,
        long effectiveCents,
        List<RefundVO> refunds) {
}
