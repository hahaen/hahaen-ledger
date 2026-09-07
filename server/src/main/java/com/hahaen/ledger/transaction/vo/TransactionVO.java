package com.hahaen.ledger.transaction.vo;

import java.time.LocalDateTime;

public record TransactionVO(
        long id,
        String transactionNo,
        String type,
        long amountCents,
        long originalAmountCents,
        boolean hasRefund,
        Long accountId,
        Long fromAccountId,
        Long toAccountId,
        LocalDateTime occurredAt,
        String note,
        String status) {
}
