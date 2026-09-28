package com.hahaen.ledger.item.vo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public record ItemDetailVO(ItemVO item, LocalDate asOf, List<CostPoint> costHistory) {
    public record CostPoint(LocalDate date, long day, BigDecimal dailyCostCents) {}
}
