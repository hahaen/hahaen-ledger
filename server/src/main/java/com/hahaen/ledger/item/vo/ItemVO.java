package com.hahaen.ledger.item.vo;
import java.math.BigDecimal;
import java.time.LocalDate;
public record ItemVO(String id, String name, long priceCents, LocalDate purchasedOn,
    String status, LocalDate retiredOn, Long resaleCents, long serviceDays,
    long netCostCents, BigDecimal dailyCostCents) {}
