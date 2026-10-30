package com.hahaen.ledger.lottery;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.databind.*;
import com.hahaen.ledger.lottery.entity.*;
import com.hahaen.ledger.lottery.mapper.LotteryStore;
import com.hahaen.ledger.lottery.service.*;
import com.hahaen.ledger.todo.service.TodoNotificationSender;
import com.hahaen.ledger.user.service.NotificationKeyCipher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.crypto.Cipher;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 显式真实DEV/MySQL/Redis/HTTP验收；渠道发送替身只验证投递状态机，不声称真实平台送达。 */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"lottery.worker.enabled=false","logging.file.path=target/lottery-dev-logs"})
@ActiveProfiles("dev")
@EnabledIfSystemProperty(named="lottery.dev.verify",matches="true")
class LotteryDevIntegrationTest {
    @org.springframework.boot.test.web.server.LocalServerPort int port;
    @Autowired TestRestTemplate http;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;
    @Autowired LotteryStore db;
    @Autowired com.hahaen.ledger.user.mapper.AppUserMapper userMapper;
    @Autowired LotteryProcessor processor;
    @Autowired LotteryDeliveryService deliveries;
    @Autowired LotteryOfficialClient official;
    @Autowired LotterySyncService sync;
    @Autowired NotificationKeyCipher cipher;
    @MockitoBean TodoNotificationSender sender;
    @MockitoBean com.hahaen.ledger.todo.service.TodoReminderWorker unrelatedTodoWorker;

    @Test void officialSourcesAndPersistentRevisions() {
        for(String game:List.of("DLT","SSQ")) {
            var page=official.fetch(game,2);assertEquals(30,page.items().size());assertTrue(page.pages()>1);assertNotNull(page.items().getFirst().draw().getNextIssue(),"官方分页首行必须连接上一页");
            sync.sync(game);long before=db.count(LotteryDraw.class,q->q.eq("game",game));assertTrue(before>=30);
            sync.sync(game);assertTrue(db.count(LotteryDraw.class,q->q.eq("game",game))>=before);
            assertTrue(db.count(LotteryDrawRevision.class,q->{})>=before);
        }
    }
    @Test void expiredSyncHolderCannotOverwriteNewLease() {
        LotterySyncState state=db.one(LotterySyncState.class,q->q.eq("game","DLT"));
        if(state==null){sync.sync("DLT");state=db.one(LotterySyncState.class,q->q.eq("game","DLT"));}
        assertTrue(db.claimSync("DLT","old-test-holder",30));
        LotterySyncState old=db.one(LotterySyncState.class,q->q.eq("game","DLT"));
        try {
            assertFalse(db.claimSync("DLT","new-test-holder",30));
            jdbc.update("UPDATE lottery_sync_state SET locked_until=DATE_SUB(CURRENT_TIMESTAMP(3),INTERVAL 1 SECOND) WHERE game='DLT'");
            assertTrue(db.claimSync("DLT","new-test-holder",30));old.setHistoryPage(9999);
            assertFalse(db.saveSyncProgress(old,"old-test-holder"));db.releaseSync("DLT","old-test-holder");
            assertTrue(db.hasSyncLease("DLT","new-test-holder"));
            assertNotEquals(9999,db.one(LotterySyncState.class,q->q.eq("game","DLT")).getHistoryPage());
        }finally{db.releaseSync("DLT","new-test-holder");db.releaseSync("DLT","old-test-holder");}
    }
    @Test void ordinaryUserWritesCannotGrantOrRestoreLotteryPermission() {
        long id=IdWorker.getId();var user=new com.hahaen.ledger.user.entity.AppUser();
        user.setId(id);user.setLoginAccount("lotteryqa"+id);user.setNickname("彩票验收");user.setStatus("ACTIVE");
        user.setPasswordHash("synthetic-unusable-hash");user.setLotteryEnabled(1);
        try {
            userMapper.insert(user);
            assertEquals(0,jdbc.queryForObject("SELECT lottery_enabled FROM app_user WHERE id=?",Integer.class,id),"所有业务创建路径必须使用数据库默认关闭");
            jdbc.update("UPDATE app_user SET lottery_enabled=1 WHERE id=?",id);
            var stale=userMapper.selectById(id);assertEquals(1,stale.getLotteryEnabled());
            jdbc.update("UPDATE app_user SET lottery_enabled=0 WHERE id=?",id);
            stale.setNickname("彩票验收资料修改");userMapper.updateById(stale);
            assertEquals(0,jdbc.queryForObject("SELECT lottery_enabled FROM app_user WHERE id=?",Integer.class,id),"旧资料实体不能覆盖维护者刚关闭的权限");
            stale.setLotteryEnabled(1);userMapper.updateById(stale);
            assertEquals(0,jdbc.queryForObject("SELECT lottery_enabled FROM app_user WHERE id=?",Integer.class,id),"业务Mapper不得写入权限字段");
        }finally{cleanup(id,id);}
    }
    @Test void schemaDefaultsAndEntityColumns() {
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM haji_flyway_history WHERE version='12' AND success=1",Integer.class));
        assertEquals("0",jdbc.queryForObject("SELECT COLUMN_DEFAULT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='app_user' AND COLUMN_NAME='lottery_enabled'",String.class));
        for(Class<?> type:List.of(LotteryDraw.class,LotteryDrawRevision.class,LotterySyncState.class,LotteryPlan.class,LotteryBatch.class,LotteryEntry.class,LotteryEvent.class,LotteryDelivery.class,LotteryAttempt.class)) {
            String table=type.getAnnotation(com.baomidou.mybatisplus.annotation.TableName.class).value();
            Set<String> columns=new HashSet<>(jdbc.queryForList("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?",String.class,table));
            Set<String> fields=new HashSet<>();for(Class<?> c=type;c!=Object.class;c=c.getSuperclass())for(var f:c.getDeclaredFields())if(!java.lang.reflect.Modifier.isStatic(f.getModifiers()))fields.add(f.getName().replaceAll("([A-Z])","_$1").toLowerCase(Locale.ROOT));assertEquals(columns,fields,table);
            assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND COLUMN_COMMENT=''",Integer.class,table));
        }
    }
    @Test void actualHttpPermissionsPurchasesSettlementNotificationsAndDeletion() throws Exception {
        long user=IdWorker.getId(),other=IdWorker.getId();String password=UUID.randomUUID().toString();
        for(long uid:new long[]{user,other})jdbc.update("INSERT INTO app_user(id,login_account,password_hash,nickname,status) VALUES(?,?,?,?, 'ACTIVE')",uid,"lotteryqa"+uid,encoder.encode(password),"彩票验收");
        try {
            String token=login(user,password),otherToken=login(other,password);
            assertFalse(ok(call("GET","/api/app/user/profile",null,token)).path("lotteryEnabled").asBoolean());
            assertNotEquals(0,call("GET","/api/app/lottery",null,token).path("code").asInt());
            jdbc.update("UPDATE app_user SET lottery_enabled=1 WHERE id=?",user);
            assertTrue(ok(call("GET","/api/app/user/profile",null,token)).path("lotteryEnabled").asBoolean());
            Map<String,Object> payload=payload("single-create-12345");
            String entry=ok(call("POST","/api/app/lottery/single",payload,token)).asText();
            assertEquals("SETTLED",db.get(LotteryEntry.class,Long.parseLong(entry)).getResult(),"补录已开奖期次应在返回前完成本地结算");
            assertEquals(entry,ok(call("POST","/api/app/lottery/single",payload,token)).asText());
            assertEquals(1,ok(call("GET","/api/app/lottery",null,token)).path("items").size());
            assertNotEquals(0,call("GET","/api/app/lottery/entry/"+entry,null,otherToken).path("code").asInt());
            jdbc.update("UPDATE app_user SET lottery_enabled=1 WHERE id=?",other);
            assertNotEquals(0,call("GET","/api/app/lottery/entry/"+entry,null,otherToken).path("code").asInt());
            assertNotEquals(0,call("POST","/api/app/lottery/entry/"+entry+"/delete",Map.of("idempotencyKey","other-delete-12345"),otherToken).path("code").asInt());
            LotteryDraw draw=db.one(LotteryDraw.class,q->q.eq("game","DLT").eq("issue","26123"));
            if(draw==null){sync.sync("DLT");draw=db.one(LotteryDraw.class,q->q.eq("game","DLT").eq("issue","26123"));}assertNotNull(draw);
            processor.process(user);assertEquals("SETTLED",db.get(LotteryEntry.class,Long.parseLong(entry)).getResult());
            assertEquals(575046200L,db.get(LotteryEntry.class,Long.parseLong(entry)).getPrizeCents());
            payload.put("idempotencyKey","single-edit-12345");payload.put("multiple",2);ok(call("PUT","/api/app/lottery/entry/"+entry,payload,token));processor.process(user);
            assertEquals(1150092400L,db.get(LotteryEntry.class,Long.parseLong(entry)).getPrizeCents());
            long config=IdWorker.getId();jdbc.update("INSERT INTO user_notification_config(id,user_id,notification_type,notification_key) VALUES(?,?,'BARK',?)",config,user,cipher.encrypt("synthetic-notification-key"));
            processor.process(user);when(sender.send(anyString(),anyString(),anyString(),anyString(),eq("彩票"))).thenReturn(true);deliveries.tick();
            assertNotNull(db.get(LotteryEntry.class,Long.parseLong(entry)).getAcceptedAt());
            payload.put("idempotencyKey","single-locked-edit-12345");assertNotEquals(0,call("PUT","/api/app/lottery/entry/"+entry,payload,token).path("code").asInt());
            Map<String,Object> planPayload=payload("plan-create-12345");planPayload.put("name","验收计划");String plan=ok(call("POST","/api/app/lottery/plan",planPayload,token)).asText();
            assertEquals("NO_PURCHASE",ok(call("GET","/api/app/lottery/plan/"+plan,null,token)).path("progress").path("reminderState").asText());
            Map<String,Object> buy=payload("batch-create-12345");buy.put("periods",20);String batch=ok(call("POST","/api/app/lottery/plan/"+plan+"/purchase",buy,token)).asText();
            assertEquals(batch,ok(call("POST","/api/app/lottery/plan/"+plan+"/purchase",buy,token)).asText());
            assertEquals(20,db.count(LotteryEntry.class,q->q.eq("batch_id",Long.parseLong(batch))));
            processor.process(user);assertEquals(0,db.count(LotteryEvent.class,q->q.eq("batch_id",Long.parseLong(batch)).eq("kind","EXPIRY")));
            JsonNode progress=ok(call("GET","/api/app/lottery?game=DLT&kind=plan",null,token)).path("items").get(0).path("progress");
            assertEquals(batch,progress.path("batchId").asText());assertEquals(20,progress.path("periods").asInt());
            long drawn=db.count(LotteryEntry.class,q->q.eq("user_id",user).eq("batch_id",Long.parseLong(batch)).isNotNull("draw_id"));
            assertEquals(drawn,progress.path("drawnCount").asLong());assertEquals(20-drawn,progress.path("remainingCount").asLong());
            assertTrue(progress.path("estimatedReminderDate").asText().matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"));
            LotteryEntry future=db.one(LotteryEntry.class,q->q.eq("batch_id",Long.parseLong(batch)).eq("position",20));
            Map<String,Object> futureEdit=payload("future-edit-12345");futureEdit.put("issue","");futureEdit.put("multiple",3);
            ok(call("PUT","/api/app/lottery/entry/"+future.getId(),futureEdit,token));assertEquals(600,db.get(LotteryEntry.class,future.getId()).getCostCents());
            assertEquals(0,db.count(LotteryEvent.class,q->q.eq("kind","RESULT").inSql("entry_id","SELECT id FROM lottery_entry WHERE plan_id="+Long.parseLong(plan))));
            buy.put("idempotencyKey","batch-duplicate-12345");assertNotEquals(0,call("POST","/api/app/lottery/plan/"+plan+"/purchase",buy,token).path("code").asInt());
            Map<String,Object> expiryPlan=payload("expiry-plan-create-12345");expiryPlan.put("name","单期到期验收");
            String expiryPlanId=ok(call("POST","/api/app/lottery/plan",expiryPlan,token)).asText();
            Map<String,Object> onePeriod=payload("expiry-purchase-12345");
            String expiryBatch=ok(call("POST","/api/app/lottery/plan/"+expiryPlanId+"/purchase",onePeriod,token)).asText();
            assertEquals(1,db.count(LotteryEvent.class,q->q.eq("batch_id",Long.parseLong(expiryBatch)).eq("kind","EXPIRY")));processor.process(user);
            assertEquals(1,db.count(LotteryEvent.class,q->q.eq("batch_id",Long.parseLong(expiryBatch)).eq("kind","EXPIRY")),"重复扫描不产生第二个到期事件");
            ok(call("POST","/api/app/lottery/plan/"+expiryPlanId+"/status",Map.of("status","PAUSED","idempotencyKey","expiry-pause-12345"),token));
            assertEquals("PAUSED",ok(call("GET","/api/app/lottery/plan/"+expiryPlanId,null,token)).path("progress").path("reminderState").asText());
            clearInvocations(sender);deliveries.tick();verifyNoInteractions(sender);
            ok(call("POST","/api/app/lottery/plan/"+expiryPlanId+"/status",Map.of("status","ACTIVE","idempotencyKey","expiry-resume-12345"),token));
            jdbc.update("UPDATE lottery_delivery d JOIN lottery_event e ON e.id=d.event_id SET d.next_attempt_at=? WHERE e.batch_id=?",LotteryRules.now(),Long.parseLong(expiryBatch));
            deliveries.tick();assertEquals(1,db.acceptedBatch(Long.parseLong(expiryBatch)));
            assertTrue(ok(call("GET","/api/app/lottery/plan/"+expiryPlanId,null,token)).path("awaitingRenewal").asBoolean());
            assertEquals("ACCEPTED",ok(call("GET","/api/app/lottery/plan/"+expiryPlanId,null,token)).path("progress").path("reminderState").asText());
            ok(call("POST","/api/app/lottery/plan/"+expiryPlanId+"/delete",Map.of("idempotencyKey","expiry-delete-12345"),token));
            jdbc.update("UPDATE app_user SET lottery_enabled=0 WHERE id=?",user);assertNotEquals(0,call("GET","/api/app/lottery",null,token).path("code").asInt());
            jdbc.update("UPDATE app_user SET lottery_enabled=1 WHERE id=?",user);
            ok(call("POST","/api/app/lottery/plan/"+plan+"/delete",Map.of("idempotencyKey","plan-delete-12345"),token));assertEquals(0,db.count(LotteryEntry.class,q->q.eq("plan_id",Long.parseLong(plan))));
            ok(call("POST","/api/app/lottery/entry/"+entry+"/delete",Map.of("idempotencyKey","entry-delete-12345"),token));assertEquals(0,ok(call("GET","/api/app/lottery",null,token)).path("items").size());
            assertNotNull(db.get(LotteryDraw.class,draw.getId()));
        }finally {cleanup(user,other);}
    }
    @Test void unknownRetryDoesNotUnlockAndOfficialCorrectionsStillSettle() throws Exception {
        long user=IdWorker.getId(),fixture=IdWorker.getId();String password=UUID.randomUUID().toString();
        jdbc.update("INSERT INTO app_user(id,login_account,password_hash,nickname,status,lottery_enabled) VALUES(?,?,?,?,'ACTIVE',1)",user,"lotteryqa"+user,encoder.encode(password),"彩票状态机合成验收");
        LotteryDraw d=new LotteryDraw();d.setId(fixture);d.setGame("DLT");d.setIssue("26998");d.setDrawDate(LocalDate.now(LotteryRules.ZONE));d.setRed("13,18,19,20,28");d.setBlue("09,11");d.setPrizes("{\"1\":10000}");d.setSpecialState("OFF");d.setPromotion(0);d.setSource("SYNTHETIC_TEST_FIXTURE");d.setRuleVersion("DLT_2026_26014");d.setSourceHash(LotteryRules.hash("synthetic"));d.setSyncedAt(LotteryRules.now());d.setVersion(1);
        try { db.insert(d);
            String token=login(user,password);Map<String,Object> p=payload("unknown-create-12345");p.put("issue","26998");
            long entry=Long.parseLong(ok(call("POST","/api/app/lottery/single",p,token)).asText());processor.process(user);
            LotteryEvent event=db.one(LotteryEvent.class,q->q.eq("entry_id",entry).eq("kind","RESULT"));assertNotNull(event);
            d.setPrizes("{}");d.setVersion(2);db.update(d);processor.process(user);assertEquals("REVIEW",db.get(LotteryEntry.class,entry).getResult());assertEquals(1,db.get(LotteryEvent.class,event.getId()).getCancelled());
            d.setPrizes("{\"1\":10000}");d.setVersion(3);db.update(d);processor.process(user);assertEquals(0,db.get(LotteryEvent.class,event.getId()).getCancelled());
            jdbc.update("INSERT INTO user_notification_config(id,user_id,notification_type,notification_key) VALUES(?,?,'BARK',?)",IdWorker.getId(),user,cipher.encrypt("synthetic-key"));processor.process(user);
            when(sender.send(anyString(),anyString(),anyString(),anyString(),eq("彩票"))).thenThrow(new org.springframework.web.client.ResourceAccessException("synthetic timeout"));deliveries.tick();
            LotteryDelivery delivery=db.one(LotteryDelivery.class,q->q.eq("event_id",event.getId()));assertEquals("UNKNOWN",delivery.getStatus());
            ok(call("POST","/api/app/lottery/delivery/"+delivery.getId()+"/retry",Map.of("idempotencyKey","unknown-retry-12345"),token));
            assertFalse(ok(call("GET","/api/app/lottery/entry/"+entry,null,token)).path("editable").asBoolean(),"未知发送历史不能因重试解锁");
            when(sender.send(anyString(),anyString(),anyString(),anyString(),eq("彩票"))).thenReturn(true);deliveries.tick();assertNotNull(db.get(LotteryEntry.class,entry).getAcceptedAt());
            d.setPrizes("{\"1\":20000}");d.setVersion(4);db.update(d);processor.process(user);assertEquals(20000,db.get(LotteryEntry.class,entry).getPrizeCents());
            assertEquals(1,db.count(LotteryEvent.class,q->q.eq("entry_id",entry).eq("kind","CORRECTION")));processor.process(user);assertEquals(1,db.count(LotteryEvent.class,q->q.eq("entry_id",entry).eq("kind","CORRECTION")));
            d.setPrizes("{\"1\":10000}");d.setVersion(5);db.update(d);processor.process(user);
            d.setPrizes("{\"1\":20000}");d.setVersion(6);db.update(d);processor.process(user);
            assertEquals(3,db.count(LotteryEvent.class,q->q.eq("entry_id",entry).eq("kind","CORRECTION")),"每个官方更正版本分别提醒，回到相同金额也不漏发");
            deliveries.tick(); // 排空此前更正消息，以下仅考察新事件的两个发送世代。
            Map<String,Object> latePayload=payload("late-sender-create-12345");latePayload.put("issue","26998");
            long lateEntry=Long.parseLong(ok(call("POST","/api/app/lottery/single",latePayload,token)).asText());
            LotteryEvent lateEvent=db.one(LotteryEvent.class,q->q.eq("entry_id",lateEntry).eq("kind","RESULT"));
            LotteryDelivery lateDelivery=db.one(LotteryDelivery.class,q->q.eq("event_id",lateEvent.getId()));
            var entered=new java.util.concurrent.CountDownLatch(1);var release=new java.util.concurrent.CountDownLatch(1);var calls=new java.util.concurrent.atomic.AtomicInteger();
            when(sender.send(anyString(),anyString(),anyString(),anyString(),eq("彩票"))).thenAnswer(invocation->{if(calls.incrementAndGet()==1){entered.countDown();if(!release.await(20,java.util.concurrent.TimeUnit.SECONDS))throw new IllegalStateException("test timeout");return false;}return true;});
            try(var executor=java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
                var oldWorker=executor.submit(()->deliveries.tick());
                try {
                    assertTrue(entered.await(10,java.util.concurrent.TimeUnit.SECONDS));
                    jdbc.update("UPDATE lottery_delivery SET locked_until=? WHERE id=?",LotteryRules.now().minusMinutes(1),lateDelivery.getId());
                    deliveries.tick();assertEquals("UNKNOWN",db.get(LotteryDelivery.class,lateDelivery.getId()).getStatus());
                    ok(call("POST","/api/app/lottery/delivery/"+lateDelivery.getId()+"/retry",Map.of("idempotencyKey","late-sender-retry-12345"),token));
                    deliveries.tick();assertEquals("ACCEPTED",db.get(LotteryDelivery.class,lateDelivery.getId()).getStatus());
                }finally{release.countDown();}
                oldWorker.get(10,java.util.concurrent.TimeUnit.SECONDS);
                assertEquals("ACCEPTED",db.get(LotteryDelivery.class,lateDelivery.getId()).getStatus(),"旧发送者不能覆盖新一代受理结果");
            }

        }finally{cleanup(user,user);jdbc.update("DELETE FROM lottery_draw WHERE id=?",fixture);}
    }
    @Test
    @EnabledIfSystemProperty(named="lottery.ui.verify",matches="true")
    void realH5WithDisposableAccount() throws Exception {
        long user=IdWorker.getId();String password=UUID.randomUUID().toString();
        jdbc.update("INSERT INTO app_user(id,login_account,password_hash,nickname,status) VALUES(?,?,?,?,'ACTIVE')",user,"lotteryqa"+user,encoder.encode(password),"彩票界面验收");
        try {
            assertEquals(0,jdbc.queryForObject("SELECT lottery_enabled FROM app_user WHERE id=?",Integer.class,user));
            jdbc.update("UPDATE app_user SET lottery_enabled=1 WHERE id=?",user);
            String token=login(user,password);Map<String,Object> single=payload("ui-single-12345");single.put("multiple",2);ok(call("POST","/api/app/lottery/single",single,token));
            Map<String,Object> plan=payload("ui-plan-12345");plan.put("name","固定号码计划");String id=ok(call("POST","/api/app/lottery/plan",plan,token)).asText();
            Map<String,Object> batch=payload("ui-batch-12345");batch.put("periods",20);ok(call("POST","/api/app/lottery/plan/"+id+"/purchase",batch,token));processor.process(user);
            LotteryUiVerifier.run(port,token);
        }finally{cleanup(user,user);}
    }
    @org.junit.jupiter.api.AfterEach
    void removeOrphanedDisposableAccounts() {
        // 仅清理由本功能验收创建、且已无购买记录的合成账号，避免早期失败遗留开通账号。
        List<Long> orphans=jdbc.queryForList("SELECT u.id FROM app_user u WHERE u.login_account LIKE 'lotteryqa%' AND u.nickname IN ('彩票验收','彩票状态机合成验收','彩票界面验收') AND NOT EXISTS(SELECT 1 FROM lottery_entry e WHERE e.user_id=u.id)",Long.class);
        for(long id:orphans)cleanup(id,id);
    }
    private Map<String,Object> payload(String key) {Map<String,Object> p=new HashMap<>();p.put("game","DLT");p.put("name","");p.put("red","13,18,19,20,28");p.put("blue","09,11");p.put("multiple",1);p.put("additional",false);p.put("issue","26123");p.put("periods",1);p.put("purchasedOn",LocalDate.now(LotteryRules.ZONE).toString());p.put("note","");p.put("confirmedPurchase",true);p.put("idempotencyKey",key);return p;}
    private String login(long user,String password) throws Exception {
        JsonNode key=ok(call("GET","/api/app/auth/password-key",null,null));JsonNode captcha=ok(call("GET","/api/app/auth/captcha",null,null));
        String svg=new String(Base64.getDecoder().decode(captcha.path("image").asText().split(",")[1]),StandardCharsets.UTF_8);String marker="fill='#278879'>";String answer=svg.substring(svg.lastIndexOf(marker)+marker.length(),svg.indexOf("</text>"));
        var pk=KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(key.path("publicKey").asText())));Cipher c=Cipher.getInstance("RSA/ECB/OAEPPadding");c.init(Cipher.ENCRYPT_MODE,pk,new OAEPParameterSpec("SHA-256","MGF1",MGF1ParameterSpec.SHA256,PSource.PSpecified.DEFAULT));
        return ok(call("POST","/api/app/auth/h5/login",Map.of("account","lotteryqa"+user,"encryptedPassword",Base64.getEncoder().encodeToString(c.doFinal(password.getBytes(StandardCharsets.UTF_8))),"captchaId",captcha.path("captchaId").asText(),"captchaCode",answer),null)).path("token").asText();
    }
    private JsonNode call(String method,String path,Object payload,String token) throws Exception {HttpHeaders h=new HttpHeaders();h.setContentType(MediaType.APPLICATION_JSON);if(token!=null)h.set("X-Auth-Token",token);return json.readTree(http.exchange(path,HttpMethod.valueOf(method),new HttpEntity<>(payload,h),String.class).getBody());}
    private JsonNode ok(JsonNode n){assertEquals(0,n.path("code").asInt(-1),n.path("message").asText());return n.path("data");}
    private void cleanup(long user,long other){for(String table:List.of("lottery_attempt","lottery_delivery","lottery_event","lottery_entry","lottery_batch","lottery_plan","lottery_request","user_notification_config_request","user_notification_config","app_login_log"))jdbc.update("DELETE FROM "+table+" WHERE user_id IN (?,?)",user,other);jdbc.update("DELETE FROM app_user WHERE id IN (?,?)",user,other);}
}
