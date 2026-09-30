package com.hahaen.ledger.todo;

import com.hahaen.ledger.todo.entity.TodoRule;
import com.hahaen.ledger.todo.service.TodoSchedule;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class TodoScheduleTest {
    @Test void monthlyThirtyFirstReturnsAfterShortMonth() {
        TodoRule rule = rule("MONTHLY", 1, "2027-01-31T09:15");
        LocalDateTime feb = TodoSchedule.next(rule, rule.getAnchorAt());
        assertEquals(LocalDateTime.parse("2027-02-28T09:15"), feb);
        assertEquals(LocalDateTime.parse("2027-03-31T09:15"), TodoSchedule.next(rule, feb));
    }
    @Test void leapDayReturnsInNextLeapYear() {
        TodoRule rule = rule("YEARLY", 1, "2024-02-29T18:30");
        LocalDateTime next = rule.getAnchorAt();
        assertEquals(LocalDateTime.parse("2025-02-28T18:30"), next = TodoSchedule.next(rule, next));
        assertEquals(LocalDateTime.parse("2026-02-28T18:30"), next = TodoSchedule.next(rule, next));
        assertEquals(LocalDateTime.parse("2027-02-28T18:30"), next = TodoSchedule.next(rule, next));
        assertEquals(LocalDateTime.parse("2028-02-29T18:30"), TodoSchedule.next(rule, next));
    }
    @Test void intervalsAndOneTime() {
        TodoRule interval = rule("EVERY_N_MONTHS", 3, "2026-08-31T08:00");
        assertEquals(LocalDateTime.parse("2026-11-30T08:00"), TodoSchedule.next(interval, interval.getAnchorAt()));
        TodoRule daily = rule("DAILY", 1, "2026-09-30T08:00");
        assertEquals(LocalDateTime.parse("2026-10-01T08:00"), TodoSchedule.next(daily, daily.getAnchorAt()));
        TodoRule once = rule("ONCE", 1, "2026-09-30T08:00");
        assertNull(TodoSchedule.next(once, once.getAnchorAt()));
    }
    private static TodoRule rule(String recurrence, int interval, String anchor) {
        TodoRule rule = new TodoRule();
        rule.setRecurrence(recurrence); rule.setMonthInterval(interval); rule.setAnchorAt(LocalDateTime.parse(anchor));
        return rule;
    }
}
