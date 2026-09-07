package com.hahaen.ledger.transaction.vo;

import java.time.LocalDateTime;

public record RefundVO(
        long id,
        String refundNo,
        long amountCents,
        LocalDateTime createdAt) {
}
