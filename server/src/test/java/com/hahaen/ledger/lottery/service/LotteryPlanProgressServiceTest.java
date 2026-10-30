package com.hahaen.ledger.lottery.service;

import com.hahaen.ledger.lottery.entity.*;
import com.hahaen.ledger.lottery.mapper.LotteryStore;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotteryPlanProgressServiceTest {
    private LotteryDraw draw(String issue, String date) {
        var draw = new LotteryDraw(); draw.setIssue(issue); draw.setDrawDate(LocalDate.parse(date)); return draw;
    }
    private LotteryEntry entry(int position, String issue, Long draw) {
        var entry = new LotteryEntry(); entry.setPosition(position); entry.setIssue(issue); entry.setDrawId(draw); return entry;
    }
    @Test void currentTwentyPeriodBatchUsesOfficialAnchorNotTodaysDate() {
        assertEquals(LocalDate.of(2026,11,21), LotteryPlanProgressService.estimate("DLT", "26114", 20,
                List.of(entry(1,"26114",1L),entry(10,"26123",2L)),
                List.of(draw("26114","2026-10-07"),draw("26123","2026-10-28")), null));
    }
    @Test void newerOfficialDateOverridesEarlierRegularScheduleEstimate() {
        assertEquals(LocalDate.of(2026,11,11), LotteryPlanProgressService.estimate("DLT", "26114", 12,
                List.of(entry(10,"26123",1L),entry(11,"26124",2L)),
                List.of(draw("26123","2026-10-28"),draw("26124","2026-11-09")), null));
    }
    @Test void ssqUsesTuesdayThursdaySundayAndKnownAnchorCanCrossYear() {
        assertEquals(LocalDate.of(2026,11,3), LotteryPlanProgressService.estimate("SSQ", "2026124", 3,
                List.of(entry(1,"2026124",1L)), List.of(draw("2026124","2026-10-29")), null));
        assertEquals(LocalDate.of(2027,1,4), LotteryPlanProgressService.estimate("DLT", "26150", 3,
                List.of(entry(1,"26150",1L)), List.of(draw("26150","2026-12-30")), null));
    }
    @Test void futureStartCanSkipIssuesButUnconfirmedYearOrHistoryDoesNotGuess() {
        var latest = draw("26123","2026-10-28");
        assertEquals(LocalDate.of(2026,11,2), LotteryPlanProgressService.estimate("DLT", "26125", 1, List.of(), List.of(), latest));
        assertNull(LotteryPlanProgressService.estimate("DLT", "27001", 20, List.of(), List.of(), latest));
        assertNull(LotteryPlanProgressService.estimate("DLT", "26114", 20, List.of(), List.of(), latest));
        assertNull(LotteryPlanProgressService.estimate("DLT", "26124", 20, List.of(), List.of(), null));
    }
    @Test void progressCountsDrawnWithUnknownPrizeAndNeverPromisesPausedReminder() {
        LotteryStore db = mock(LotteryStore.class);
        var service = new LotteryPlanProgressService(db);
        var plan = new LotteryPlan(); plan.setId(10L); plan.setUserId(7L); plan.setGame("DLT"); plan.setStatus("PAUSED");
        var batch = new LotteryBatch(); batch.setId(20L); batch.setPeriods(2); batch.setStartIssue("26123");
        when(db.one(eq(LotteryBatch.class),any())).thenReturn(batch);
        when(db.list(eq(LotteryEntry.class),any())).thenReturn(List.of(entry(1,"26123",1L),entry(2,null,null)));
        var progress = service.describe(plan,true);
        assertEquals(1,progress.drawnCount()); assertEquals(1,progress.remainingCount());
        assertEquals("PAUSED",progress.reminderState()); assertNull(progress.estimatedReminderDate());
        plan.setStatus("ENDED"); assertEquals("ENDED",service.describe(plan,true).reminderState());
    }
    @Test void noPurchaseAndAcceptedOrFailedDeliveryHaveExplicitStatus() {
        LotteryStore db = mock(LotteryStore.class); var service = new LotteryPlanProgressService(db);
        var plan = new LotteryPlan(); plan.setId(10L); plan.setUserId(7L); plan.setStatus("ACTIVE");
        assertEquals("NO_PURCHASE",service.describe(plan,true).reminderState());
        var batch = new LotteryBatch(); batch.setId(20L); batch.setPeriods(1); batch.setStartIssue("26123");
        var event = new LotteryEvent(); event.setId(30L);
        var delivery = new LotteryDelivery(); delivery.setStatus("ACCEPTED");
        when(db.one(eq(LotteryBatch.class),any())).thenReturn(batch);
        when(db.list(eq(LotteryEntry.class),any())).thenReturn(List.of(entry(1,"26123",1L)));
        when(db.one(eq(LotteryEvent.class),any())).thenReturn(event);
        when(db.list(eq(LotteryDelivery.class),any())).thenReturn(List.of(delivery));
        assertEquals("ACCEPTED",service.describe(plan,true).reminderState());
        delivery.setStatus("FAILED"); assertEquals("FAILED",service.describe(plan,true).reminderState());
        delivery.setStatus("UNKNOWN"); assertEquals("UNKNOWN",service.describe(plan,true).reminderState());
        delivery.setStatus("PENDING"); assertEquals("DUE",service.describe(plan,true).reminderState());
        assertEquals("NO_CHANNEL",service.describe(plan,false).reminderState());
    }
}
