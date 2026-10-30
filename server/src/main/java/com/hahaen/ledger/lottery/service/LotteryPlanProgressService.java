package com.hahaen.ledger.lottery.service;

import com.hahaen.ledger.lottery.entity.*;
import com.hahaen.ledger.lottery.mapper.LotteryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/** 仅计算展示进度；预计日期不参与期号绑定或真实提醒调度。 */
@Service
@RequiredArgsConstructor
public class LotteryPlanProgressService {
    private final LotteryStore db;

    public record Progress(String batchId, String startIssue, int periods, int drawnCount,
                           int remainingCount, LocalDate estimatedReminderDate, String reminderState) {}

    @Transactional(readOnly = true)
    public Progress describe(LotteryPlan plan, boolean notificationConfigured) {
        LotteryBatch batch = db.one(LotteryBatch.class, q -> q.eq("user_id", plan.getUserId())
                .eq("plan_id", plan.getId()).orderByDesc("created_at", "id"));
        String inactive = "ACTIVE".equals(plan.getStatus()) ? null : plan.getStatus();
        if (batch == null) return new Progress(null, null, 0, 0, 0, null,
                inactive == null ? "NO_PURCHASE" : inactive);
        List<LotteryEntry> entries = db.list(LotteryEntry.class, q -> q.eq("user_id", plan.getUserId())
                .eq("batch_id", batch.getId()).orderByAsc("position"));
        int drawn = (int) entries.stream().filter(e -> e.getDrawId() != null).count();
        int remaining = Math.max(0, batch.getPeriods() - drawn);
        LocalDate estimate = null;
        String state = inactive;
        if (state == null) {
            LotteryEvent expiry = db.one(LotteryEvent.class, q -> q.eq("user_id", plan.getUserId())
                    .eq("batch_id", batch.getId()).eq("kind", "EXPIRY").eq("cancelled", 0));
            List<LotteryDelivery> deliveries = expiry == null ? List.of() : db.list(LotteryDelivery.class,
                    q -> q.eq("user_id", plan.getUserId()).eq("event_id", expiry.getId()));
            boolean due = entries.size() == batch.getPeriods() && !entries.isEmpty()
                    && entries.getLast().getPosition().equals(batch.getPeriods())
                    && entries.getLast().getDrawId() != null;
            if (deliveries.stream().anyMatch(d -> "ACCEPTED".equals(d.getStatus()))) state = "ACCEPTED";
            else if (!notificationConfigured) state = "NO_CHANNEL";
            else if (due) {
                if (deliveries.stream().anyMatch(d -> "UNKNOWN".equals(d.getStatus()))) state = "UNKNOWN";
                else if (!deliveries.isEmpty() && deliveries.stream().allMatch(d -> "FAILED".equals(d.getStatus()))) state = "FAILED";
                else state = "DUE";
            } else {
                List<String> issues = entries.stream().map(LotteryEntry::getIssue).filter(Objects::nonNull).distinct().toList();
                List<LotteryDraw> known = issues.isEmpty() ? List.of() : db.list(LotteryDraw.class,
                        q -> q.eq("game", plan.getGame()).in("issue", issues));
                LotteryDraw latest = db.one(LotteryDraw.class, q -> q.eq("game", plan.getGame()).orderByDesc("issue"));
                estimate = estimate(plan.getGame(), batch.getStartIssue(), batch.getPeriods(), entries, known, latest);
                state = estimate == null ? "UNCONFIRMED" : estimate.isBefore(LocalDate.now(LotteryRules.ZONE)) ? "WAITING_SYNC" : "WAITING";
            }
        }
        return new Progress(batch.getId().toString(), batch.getStartIssue(), batch.getPeriods(), drawn, remaining, estimate, state);
    }

    static LocalDate estimate(String game, String startIssue, int periods, List<LotteryEntry> entries,
                              List<LotteryDraw> known, LotteryDraw latest) {
        Map<String, LocalDate> dates = new HashMap<>();
        for (LotteryDraw draw : known) if (draw.getDrawDate() != null) dates.put(draw.getIssue(), draw.getDrawDate());
        LocalDate anchor = null;
        int position = 0;
        for (LotteryEntry entry : entries) {
            LocalDate date = dates.get(entry.getIssue());
            if (date != null && entry.getPosition() > position) { anchor = date; position = entry.getPosition(); }
        }
        if (anchor != null) return advance(game, anchor, periods - position);
        if (latest == null || latest.getDrawDate() == null || startIssue == null) return null;
        int yearLength = "DLT".equals(game) ? 2 : 4;
        // 起始期跨年或早于缓存时无法确定休市/年度期数，不用数字相减伪造日期。
        if (startIssue.length() != yearLength + 3 || latest.getIssue().length() != startIssue.length()
                || !startIssue.substring(0, yearLength).equals(latest.getIssue().substring(0, yearLength))) return null;
        int gap = Integer.parseInt(startIssue.substring(yearLength)) - Integer.parseInt(latest.getIssue().substring(yearLength));
        if (gap <= 0 || gap > 366) return null;
        return advance(game, latest.getDrawDate(), gap + periods - 1);
    }

    private static LocalDate advance(String game, LocalDate date, int periods) {
        Set<DayOfWeek> days = "DLT".equals(game)
                ? EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY)
                : EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY);
        while (periods > 0) { date = date.plusDays(1); if (days.contains(date.getDayOfWeek())) periods--; }
        return date;
    }
}
