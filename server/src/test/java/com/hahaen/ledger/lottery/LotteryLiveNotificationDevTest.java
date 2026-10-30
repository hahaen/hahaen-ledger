package com.hahaen.ledger.lottery;

import com.hahaen.ledger.lottery.service.LotteryRules;
import com.hahaen.ledger.todo.service.TodoNotificationSender;
import com.hahaen.ledger.user.service.NotificationKeyCipher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** 显式人工启用：向已确认的既有DEV账号发送验收消息，不修改用户权限，不输出通知凭证。 */
@SpringBootTest(properties={"lottery.worker.enabled=false","logging.file.path=target/lottery-live-notification-logs"})
@ActiveProfiles("dev")
@EnabledIfSystemProperty(named="lottery.live.verify",matches="true")
class LotteryLiveNotificationDevTest {
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    com.hahaen.ledger.todo.service.TodoReminderWorker unrelatedTodoWorker;
    @Autowired JdbcTemplate jdbc;
    @Autowired NotificationKeyCipher cipher;
    @Autowired TodoNotificationSender sender;
    @Test void existingConfiguredChannelsAcceptOneVerificationMessage() {
        String account=System.getProperty("lottery.live.account","");
        assertFalse(account.isBlank(),"真实推送必须显式指定接收账号");
        List<Long> owners=jdbc.queryForList("SELECT id FROM app_user WHERE login_account=? AND status='ACTIVE' AND deleted=0",Long.class,account);
        assertEquals(1,owners.size(),"接收账号必须唯一有效");long user=owners.getFirst();
        Integer permission=jdbc.queryForObject("SELECT lottery_enabled FROM app_user WHERE id=?",Integer.class,user);
        var rows=jdbc.queryForList("SELECT notification_type,notification_key FROM user_notification_config WHERE user_id=? AND deleted=0 AND notification_type IN ('BARK','PUSHPLUS') ORDER BY notification_type",user);
        assertFalse(rows.isEmpty(),"接收账号尚未配置提醒渠道");
        String body="哈记账：彩票功能验收\n这是一条功能测试消息，无需操作。\n验证时间："+LotteryRules.now()+"\n本次测试没有开通账号的彩票权限。";
        for(var row:rows) {
            String channel=(String)row.get("notification_type");
            boolean accepted;
            try {accepted=sender.send(channel,cipher.decrypt((String)row.get("notification_key")),"彩票功能验收",body,"彩票");}
            catch(RuntimeException ex){throw new AssertionError("真实通知失败，渠道="+channel+"，类别="+ex.getClass().getSimpleName());}
            assertTrue(accepted,"真实通知未被渠道受理："+channel);
            System.out.println("LOTTERY_LIVE_NOTIFICATION "+channel+" ACCEPTED (device delivery not asserted)");
        }
        assertEquals(permission,jdbc.queryForObject("SELECT lottery_enabled FROM app_user WHERE id=?",Integer.class,user),"推送验收不得修改彩票权限");
    }
}
