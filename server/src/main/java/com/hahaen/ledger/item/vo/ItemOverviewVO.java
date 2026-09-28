package com.hahaen.ledger.item.vo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public record ItemOverviewVO(LocalDate asOf, long totalAssetsCents, BigDecimal totalDailyCostCents,
    long activeCount, long retiredCount, List<ItemVO> items, long total, int page, int pageSize, boolean hasMore) {}
