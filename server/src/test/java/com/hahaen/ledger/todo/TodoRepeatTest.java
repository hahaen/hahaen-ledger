package com.hahaen.ledger.todo;

import com.hahaen.ledger.todo.entity.TodoRule;
import com.hahaen.ledger.todo.service.TodoRepeat;
import com.hahaen.ledger.todo.service.TodoSchedule;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class TodoRepeatTest {
    private TodoRule rule(String unit, int interval, String anchor) {
        TodoRule r = new TodoRule(); r.setRecurrence("CUSTOM"); r.setRepeatMode("TIME");
        r.setRepeatUnit(unit); r.setRepeatInterval(interval); r.setAnchorAt(LocalDateTime.parse(anchor));
        return r;
    }
    @Test void dailyIntervalAndCutoff() {
        TodoRule r = rule("DAY", 365, "2027-01-01T09:00");
        TodoRepeat.validate(r);
        assertEquals(LocalDateTime.parse("2028-01-01T09:00"), TodoSchedule.next(r, r.getAnchorAt()));
        r.setAnchorAt(LocalDateTime.parse("2099-12-31T09:00"));
        assertNull(TodoSchedule.next(r, r.getAnchorAt()));
    }
    @Test void weeklyMultipleDaysAndSkippedWeeks() {
        TodoRule r = rule("WEEK", 2, "2026-09-30T09:00"); r.setWeekDays("1,5");
        TodoRepeat.validate(r);
        LocalDateTime first = TodoRepeat.first(r, r.getAnchorAt());
        assertEquals(LocalDateTime.parse("2026-10-02T09:00"), first);
        assertEquals(LocalDateTime.parse("2026-10-12T09:00"), TodoSchedule.next(r, first));
        assertEquals(LocalDateTime.parse("2026-10-16T09:00"), TodoSchedule.next(r, LocalDateTime.parse("2026-10-12T09:00")));
    }
    @Test void monthlyDuplicatesCollapseAndAnchorSurvives() {
        TodoRule r = rule("MONTH", 1, "2027-01-30T08:00"); r.setMonthDays("15,30,31"); r.setLastDay(1);
        TodoRepeat.validate(r);
        LocalDateTime jan = TodoSchedule.next(r, r.getAnchorAt());
        assertEquals(LocalDateTime.parse("2027-01-31T08:00"), jan);
        LocalDateTime feb = TodoSchedule.next(r, jan);
        assertEquals(LocalDateTime.parse("2027-02-15T08:00"), feb);
        feb = TodoSchedule.next(r, feb);
        assertEquals(LocalDateTime.parse("2027-02-28T08:00"), feb);
        assertEquals(LocalDateTime.parse("2027-03-15T08:00"), TodoSchedule.next(r, feb));
        r.setRepeatInterval(3);
        assertEquals(LocalDateTime.parse("2027-04-15T08:00"), TodoSchedule.next(r, jan));
    }
    @Test void yearlyMultipleDatesAndLeapYears() {
        TodoRule r = rule("YEAR", 2, "2026-01-01T08:00"); r.setYearDays("02-28,02-29,09-30");
        TodoRepeat.validate(r);
        assertEquals(LocalDateTime.parse("2026-02-28T08:00"), TodoRepeat.first(r, r.getAnchorAt()));
        assertEquals(LocalDateTime.parse("2026-09-30T08:00"), TodoSchedule.next(r, LocalDateTime.parse("2026-02-28T08:00")));
        assertEquals(LocalDateTime.parse("2028-02-28T08:00"), TodoSchedule.next(r, LocalDateTime.parse("2026-09-30T08:00")));
        assertEquals(LocalDateTime.parse("2028-02-29T08:00"), TodoSchedule.next(r, LocalDateTime.parse("2028-02-28T08:00")));
    }
    @Test void completionBasedDoesNotPreGenerate() {
        for (String unit : new String[]{"DAY","WEEK","MONTH","YEAR"}) {
            TodoRule r = rule(unit, 1, "2027-01-01T08:00"); r.setRepeatMode("AFTER_COMPLETION");
            TodoRepeat.validate(r);
            assertNull(TodoSchedule.next(r, r.getAnchorAt()));
            LocalDateTime completed = LocalDateTime.parse("2027-01-31T18:45");
            LocalDateTime expected = switch(unit) { case "DAY" -> completed.plusDays(1); case "WEEK" -> completed.plusWeeks(1); case "MONTH" -> completed.plusMonths(1); default -> completed.plusYears(1); };
            assertEquals(expected, TodoRepeat.completedNext(r, completed));
        }
    }
    @Test void fixedDatesAreSortedFiniteAndRejectInvalidDates() {
        TodoRule r = rule(null, 1, "2027-01-01T08:00"); r.setRepeatMode("FIXED_DATES"); r.setRepeatInterval(null);
        r.setFixedDates("2027-09-30,2027-01-02"); TodoRepeat.validate(r);
        LocalDateTime first = TodoRepeat.first(r, r.getAnchorAt());
        assertEquals(LocalDateTime.parse("2027-01-02T08:00"), first);
        LocalDateTime last = TodoSchedule.next(r, first);
        assertEquals(LocalDateTime.parse("2027-09-30T08:00"), last); assertNull(TodoSchedule.next(r, last));
        r.setFixedDates("2027-02-29"); assertThrows(RuntimeException.class, () -> TodoRepeat.validate(r));
    }
    @Test void invalidSelectionsAndBounds() {
        TodoRule r = rule("WEEK", 1, "2027-01-01T08:00");
        assertThrows(RuntimeException.class, () -> TodoRepeat.validate(r));
        r.setWeekDays("8"); assertThrows(RuntimeException.class, () -> TodoRepeat.validate(r));
        r.setWeekDays("1,1"); assertThrows(RuntimeException.class, () -> TodoRepeat.validate(r));
        r.setWeekDays("1"); r.setRepeatInterval(366); assertThrows(RuntimeException.class, () -> TodoRepeat.validate(r));
        r.setRepeatInterval(1); r.setMonthDays("1"); assertThrows(RuntimeException.class, () -> TodoRepeat.validate(r));
    }
}
