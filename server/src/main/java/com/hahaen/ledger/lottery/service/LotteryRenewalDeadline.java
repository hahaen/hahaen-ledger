package com.hahaen.ledger.lottery.service;

import com.hahaen.ledger.lottery.entity.LotteryDraw;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** 续买正文的辅助说明，不参与期号绑定、购买或任务调度。 */
public final class LotteryRenewalDeadline {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private LotteryRenewalDeadline() {}

    public static String describe(String game, LotteryDraw last, LotteryDraw next, LocalDateTime now) {
        LotteryRules.game(game);
        boolean confirmed = next != null && next.getDrawDate() != null;
        LocalDate date = confirmed ? next.getDrawDate() : last == null ? null : last.getDrawDate();
        if (date == null) return "下期购买截止：待核实，请查看官方公告及网点实际销售时间。";
        if (!confirmed) {
            Set<DayOfWeek> days = "DLT".equals(game)
                    ? EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY)
                    : EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY);
            do { date = date.plusDays(1); } while (!days.contains(date.getDayOfWeek()));
        }
        LocalDateTime deadline = date.atTime("DLT".equals(game) ? 21 : 20, 0);
        String weekday = List.of("周一", "周二", "周三", "周四", "周五", "周六", "周日")
                .get(date.getDayOfWeek().getValue() - 1);
        return "下期购买截止" + (confirmed ? "" : "（预计）") + "：" + date + "（" + weekday + "）"
                + deadline.format(TIME) + "（北京时间）" + (!now.isBefore(deadline) ? "（已过截止时间）" : "")
                + "\n" + (confirmed ? "" : "按常规开奖日推算；") + "休市、延期以官方公告及网点实际销售为准。";
    }
}
