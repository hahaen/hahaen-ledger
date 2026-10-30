package com.hahaen.ledger.lottery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hahaen.ledger.lottery.entity.*;
import com.hahaen.ledger.lottery.service.*;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class LotteryRulesTest {
    private final ObjectMapper json=new ObjectMapper();
    @Test void schedulerHasNoInventedUserIdentity() {
        assertNull(com.hahaen.ledger.common.security.CurrentUser.optionalId());
        assertNull(com.hahaen.ledger.common.security.CurrentUser.optionalName());
        assertThrows(com.hahaen.ledger.common.exception.BusinessException.class,com.hahaen.ledger.common.security.CurrentUser::id);
    }
    @Test void strictNumbersCostAndUnknownMoney() {
        assertEquals("01,03,05,20,35",LotteryRules.red("DLT","35 5 01 20 3"));
        assertThrows(RuntimeException.class,()->LotteryRules.red("DLT","1,1,2,3,4"));
        assertThrows(RuntimeException.class,()->LotteryRules.blue("SSQ","17"));
        assertThrows(RuntimeException.class,()->LotteryRules.issue("SSQ","26124"));
        assertThrows(RuntimeException.class,()->LotteryRules.cost("SSQ",1,true));
        assertThrows(RuntimeException.class,()->LotteryRules.cost("DLT",100,false));
        assertEquals(29700,LotteryRules.cost("DLT",99,true));
        assertEquals(460036900L,LotteryRules.money("4,600,369"));
        assertNull(LotteryRules.money(""));assertNull(LotteryRules.money("-"));assertNull(LotteryRules.money("1.001"));
    }
    @Test void currentDltOfficialPrizeAndAdditionalAmounts() throws Exception {
        LotteryEntry e=entry("DLT","13,18,19,20,28","09,11");e.setMultiple(2);e.setAdditional(1);
        LotteryDraw d=draw("DLT","26123",e.getRed(),e.getBlue());
        var prizes=json.readTree("{\"1\":575046200,\"A1\":460036900,\"3\":500000,\"7\":500,\"9\":500}");
        assertEquals(2070166200L,LotteryRules.settle(e,d,prizes).cents());
        assertEquals("一等奖",LotteryRules.settle(e,d,prizes).grade());
        assertNull(LotteryRules.settle(e,d,json.readTree("{\"1\":575046200}")).cents());
        e.setRed("01,02,03,04,05");e.setBlue("01,02");assertEquals(0,LotteryRules.settle(e,d,prizes).cents());
        e.setRed("13,18,19,01,02");e.setBlue("01,02");assertEquals("七等奖",LotteryRules.settle(e,d,prizes).grade());
        d.setIssue("25001");assertEquals("九等奖",LotteryRules.settle(e,d,prizes).grade());
        d.setIssue("26123");d.setPromotion(1);assertEquals("REVIEW",LotteryRules.settle(e,d,prizes).status());
    }
    @Test void ssqSpecialStateAndHighestPrizeNotStacked() throws Exception {
        LotteryDraw d=draw("SSQ","2026124","03,11,17,22,23,29","09");
        LotteryEntry e=entry("SSQ","01,02,03,11,17,30","08");
        var prizes=json.readTree("{\"5\":1000,\"6\":500,\"7\":500,\"9\":500}");
        d.setSpecialState("UNKNOWN");assertEquals("REVIEW",LotteryRules.settle(e,d,prizes).status());
        d.setSpecialState("OFF");assertEquals(0,LotteryRules.settle(e,d,prizes).cents());
        d.setSpecialState("ON");assertEquals("福运奖",LotteryRules.settle(e,d,prizes).grade());assertEquals(500,LotteryRules.settle(e,d,prizes).cents());
        e.setBlue("09");assertEquals(1000,LotteryRules.settle(e,d,prizes).cents());
        d.setIssue("2025010");e.setBlue("08");assertEquals(0,LotteryRules.settle(e,d,prizes).cents());
    }
    @Test void parserKeepsMissingAmountsAndRejectsMalformedDraws() throws Exception {
        LotteryOfficialClient client=new LotteryOfficialClient(json);
        var row=json.readTree("{\"code\":\"2026124\",\"date\":\"2026-10-29(四)\",\"red\":\"03,11,17,22,23,29\",\"blue\":\"09\",\"prizegrades\":[{\"type\":1,\"typemoney\":\"6656899\"},{\"type\":7,\"typemoney\":\"\"}]}");
        LotteryDraw d=client.parse("SSQ",row).draw();assertEquals("UNKNOWN",d.getSpecialState());assertFalse(json.readTree(d.getPrizes()).has("7"));
        assertThrows(RuntimeException.class,()->client.parse("SSQ",json.valueToTree(Map.of("code","2026124","date","2026-10-29","red","01,01,02,03,04,05","blue","09"))));
    }
    private LotteryEntry entry(String game,String red,String blue){var e=new LotteryEntry();e.setGame(game);e.setRed(red);e.setBlue(blue);e.setMultiple(1);e.setAdditional(0);return e;}
    private LotteryDraw draw(String game,String issue,String red,String blue){var d=new LotteryDraw();d.setGame(game);d.setIssue(issue);d.setRed(red);d.setBlue(blue);d.setPromotion(0);d.setSpecialState("OFF");return d;}
}
