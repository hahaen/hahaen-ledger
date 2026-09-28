package com.hahaen.ledger.item.service;
import com.hahaen.ledger.item.entity.PersonalItem;
import com.hahaen.ledger.item.vo.ItemVO;
import com.hahaen.ledger.item.vo.ItemDetailVO.CostPoint;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public final class ItemCosts {
    private ItemCosts() {}
    public static BigDecimal average(long cents, long days) {
        return BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(days), 8, RoundingMode.HALF_UP);
    }
    public static ItemVO view(PersonalItem item, LocalDate today) {
        LocalDate end = "RETIRED".equals(item.getStatus()) ? item.getRetiredOn() : today;
        long days = ChronoUnit.DAYS.between(item.getPurchasedOn(), end) + 1;
        long net = item.getPriceCent() - ("RETIRED".equals(item.getStatus()) ? item.getResaleCent() : 0);
        return new ItemVO(String.valueOf(item.getId()), item.getName(), item.getPriceCent(), item.getPurchasedOn(),
            item.getStatus(), item.getRetiredOn(), item.getResaleCent(), days, net, average(net, days));
    }
    public static List<CostPoint> history(PersonalItem item, LocalDate today) {
        ItemVO view = view(item, today);
        int count = (int) Math.min(31, view.serviceDays());
        List<CostPoint> points = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            long day = count == 1 ? 1 : 1 + i * (view.serviceDays() - 1) / (count - 1);
            long cost = day == view.serviceDays() ? view.netCostCents() : view.priceCents();
            points.add(new CostPoint(item.getPurchasedOn().plusDays(day - 1), day, average(cost, day)));
        }
        return points;
    }
}
