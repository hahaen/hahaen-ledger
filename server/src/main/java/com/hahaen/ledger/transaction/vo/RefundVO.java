package com.hahaen.ledger.transaction.vo;

import java.time.LocalDateTime;

public record RefundVO(
        String id,
        String refundNo,
        long amountCents,
        LocalDateTime createdAt) {
}
