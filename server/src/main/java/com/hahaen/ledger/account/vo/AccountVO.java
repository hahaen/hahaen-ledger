package com.hahaen.ledger.account.vo;

public record AccountVO(
        String id,
        String name,
        String kind,
        int sortOrder,
        long balanceCents,
        long creditLimitCents,
        boolean includedInNetAsset,
        String status) {
}
