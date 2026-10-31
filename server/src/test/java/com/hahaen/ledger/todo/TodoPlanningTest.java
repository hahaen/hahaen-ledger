package com.hahaen.ledger.todo;

import com.hahaen.ledger.todo.dto.TodoPlanRequest;
import com.hahaen.ledger.todo.entity.TodoOccurrence;
import com.hahaen.ledger.todo.entity.TodoRule;
import com.hahaen.ledger.todo.service.*;
import com.hahaen.ledger.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class TodoPlanningTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026,10,31,16,0);
    private TodoPlanRequest plan(String mode, String unit, int interval, String date, String week, String month, boolean last, String year, String fixed) {
        return new TodoPlanRequest("CUSTOM",1,LocalDateTime.parse(date),mode,unit,interval,week,month,last,year,fixed);
    }
    @Test void monthlyStartAndExecutionTimeUseSameRuleForPreviewAndSave() {
        TodoRule r = TodoPlanning.rule(plan("TIME","MONTH",1,"2026-10-31T08:00:00",null,"1",false,null,null));
        var first = TodoPlanning.first(r,null,NOW);
        assertEquals(LocalDateTime.parse("2026-11-01T08:00:00"), first);
        assertEquals(LocalDateTime.parse("2026-12-01T08:00:00"), TodoSchedule.next(r,first));
        assertEquals(LocalDateTime.parse("2026-10-31T08:00:00"),r.getAnchorAt());
    }
    @Test void todayPastTimeSkipsPassedOccurrenceButPreservesDayIntervalAnchor() {
        TodoRule r = TodoPlanning.rule(plan("TIME","DAY",2,"2026-10-31T08:00:00",null,null,false,null,null));
        assertEquals(LocalDateTime.parse("2026-11-02T08:00:00"),TodoPlanning.first(r,null,NOW));
    }
    @Test void oldPendingPrecisionAndBlankSelectionsDoNotReanchor() {
        TodoRule r = TodoPlanning.rule(plan("TIME","MONTH",3,"2020-01-31T09:00:12.345",null,"31",false,null,null));
        r.setWeekDays("");
        TodoOccurrence occurrence = new TodoOccurrence(); occurrence.setDueAt(LocalDateTime.parse("2020-04-30T09:00:12.345"));
        TodoRule same = TodoPlanning.rule(plan("TIME","MONTH",3,occurrence.getDueAt().toString(),null,"31",false,null,null));
        assertTrue(TodoPlanning.unchanged(r,occurrence,same));
        same.setRepeatInterval(2); assertFalse(TodoPlanning.unchanged(r,occurrence,same));
        assertThrows(BusinessException.class,()->TodoPlanning.first(same,null,NOW));
    }
    @Test void fixedDatesSortedAndExhaustedAfterLast() {
        TodoRule r = TodoPlanning.rule(new TodoPlanRequest("CUSTOM",1,LocalDateTime.parse("2026-11-01T08:30:00"),"FIXED_DATES",null,null,null,null,false,null,"2026-12-01,2026-11-01"));
        var first=TodoPlanning.first(r,null,NOW);
        assertEquals(LocalDateTime.parse("2026-11-01T08:30:00"),first);
        var next=TodoSchedule.next(r,first);
        assertEquals(LocalDateTime.parse("2026-12-01T08:30:00"),next); assertNull(TodoSchedule.next(r,next));
    }
    @Test void onceAndAfterCompletionDoNotInventFutureTimes() {
        TodoRule once=TodoPlanning.rule(new TodoPlanRequest("ONCE",1,NOW.plusHours(1),null,null,null,null,null,false,null,null));
        assertNull(TodoSchedule.next(once,TodoPlanning.first(once,null,NOW)));
        TodoRule after=TodoPlanning.rule(plan("AFTER_COMPLETION","MONTH",3,"2026-11-01T08:00:00",null,null,false,null,null));
        assertNull(TodoSchedule.next(after,TodoPlanning.first(after,null,NOW)));
        assertEquals(NOW.plusMonths(3),TodoRepeat.completedNext(after,NOW));
    }
    @Test void weeklyMonthEndLeapYearAndUpperBound() {
        TodoRule week=TodoPlanning.rule(plan("TIME","WEEK",2,"2026-10-31T18:00:00","1,6",null,false,null,null));
        assertEquals(LocalDateTime.parse("2026-10-31T18:00:00"),TodoPlanning.first(week,null,NOW));
        assertEquals(LocalDateTime.parse("2026-11-09T18:00:00"),TodoSchedule.next(week,LocalDateTime.parse("2026-10-31T18:00:00")));
        TodoRule month=TodoPlanning.rule(plan("TIME","MONTH",1,"2027-02-01T09:00:00",null,"31",true,null,null));
        assertEquals(LocalDateTime.parse("2027-02-28T09:00:00"),TodoPlanning.first(month,null,NOW));
        TodoRule year=TodoPlanning.rule(plan("TIME","YEAR",1,"2027-01-01T09:00:00",null,null,false,"02-29",null));
        assertEquals(LocalDateTime.parse("2027-02-28T09:00:00"),TodoPlanning.first(year,null,NOW));
        TodoRule limit=TodoPlanning.rule(plan("TIME","MONTH",1,"2099-12-31T09:00:00",null,"1",false,null,null));
        assertThrows(BusinessException.class,()->TodoPlanning.first(limit,null,NOW));
    }
}
