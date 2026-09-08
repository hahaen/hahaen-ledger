package com.hahaen.ledger.transaction.vo;

import java.time.LocalDateTime;

public record TransactionVO(
        String id,
        String transactionNo,
        String type,
        long amountCents,
        long originalAmountCents,
        boolean hasRefund,
        String accountId,
        String fromAccountId,
        String toAccountId,
        LocalDateTime occurredAt,
        String note,
        String status) {
}
