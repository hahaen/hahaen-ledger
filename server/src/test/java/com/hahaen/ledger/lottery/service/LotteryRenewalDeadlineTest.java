package com.hahaen.ledger.lottery.service;

import com.hahaen.ledger.lottery.entity.LotteryDraw;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class LotteryRenewalDeadlineTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 30, 12, 0);
    private LotteryDraw draw(String date) {
        var draw = new LotteryDraw(); draw.setDrawDate(LocalDate.parse(date)); return draw;
    }

    @Test void dltIncludesSaturdayAndNinePmBeijingTime() {
        assertEquals("下期购买截止（预计）：2026-10-31（周六）21:00（北京时间）\n按常规开奖日推算；休市、延期以官方公告及网点实际销售为准。",
                LotteryRenewalDeadline.describe("DLT", draw("2026-10-28"), null, NOW));
    }
    @Test void ssqIncludesSundayAndEightPm() {
        assertTrue(LotteryRenewalDeadline.describe("SSQ", draw("2026-10-29"), null, NOW)
                .startsWith("下期购买截止（预计）：2026-11-01（周日）20:00（北京时间）"));
    }
    @Test void eachRegularWeekdayAndYearBoundaryAreCorrect() {
        for (String[] pair : new String[][]{{"2026-10-31","2026-11-02（周一）"},{"2026-11-02","2026-11-04（周三）"},
                {"2026-12-30","2027-01-02（周六）"}})
            assertTrue(LotteryRenewalDeadline.describe("DLT", draw(pair[0]), null, NOW).contains(pair[1]));
        for (String[] pair : new String[][]{{"2026-11-01","2026-11-03（周二）"},{"2026-11-03","2026-11-05（周四）"}})
            assertTrue(LotteryRenewalDeadline.describe("SSQ", draw(pair[0]), null, NOW).contains(pair[1]));
    }
    @Test void actualAdjacentDrawOverridesRegularScheduleAfterPostponement() {
        String body = LotteryRenewalDeadline.describe("DLT", draw("2026-10-28"), draw("2026-11-09"), NOW);
        assertTrue(body.startsWith("下期购买截止：2026-11-09（周一）21:00（北京时间）"));
        assertFalse(body.contains("预计")); assertFalse(body.contains("常规"));
    }
    @Test void delayedProcessingDoesNotJumpToAnotherIssueAndExactDeadlineIsClosed() {
        LocalDateTime deadline = LocalDateTime.of(2026, 10, 31, 21, 0);
        assertFalse(LotteryRenewalDeadline.describe("DLT", draw("2026-10-28"), null, deadline.minusNanos(1)).contains("已过"));
        for (LocalDateTime now : new LocalDateTime[]{deadline, deadline.plusDays(10)}) {
            String body = LotteryRenewalDeadline.describe("DLT", draw("2026-10-28"), null, now);
            assertTrue(body.contains("2026-10-31（周六）21:00（北京时间）（已过截止时间）"));
        }
    }
    @Test void missingOfficialAnchorDoesNotInventDeadline() {
        assertEquals("下期购买截止：待核实，请查看官方公告及网点实际销售时间。",
                LotteryRenewalDeadline.describe("SSQ", null, null, NOW));
        assertTrue(LotteryRenewalDeadline.describe("DLT", new LotteryDraw(), null, NOW).contains("待核实"));
    }
}
