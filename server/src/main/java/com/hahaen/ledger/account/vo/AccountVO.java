package com.hahaen.ledger.account.vo;

public record AccountVO(
        long id,
        String name,
        String kind,
        long balanceCents,
        long creditLimitCents,
        boolean includedInNetAsset,
        String status) {
}
