package com.hahaen.ledger.todo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.hahaen.ledger.todo.entity.TodoDelivery;
import com.hahaen.ledger.common.security.AuditIdentity;
import com.hahaen.ledger.todo.entity.TodoDeliveryAttempt;
import com.hahaen.ledger.todo.entity.TodoOccurrence;
import com.hahaen.ledger.todo.entity.TodoRule;
import com.hahaen.ledger.todo.mapper.TodoDeliveryAttemptMapper;
import com.hahaen.ledger.todo.mapper.TodoDeliveryMapper;
import com.hahaen.ledger.todo.mapper.TodoOccurrenceMapper;
import com.hahaen.ledger.todo.mapper.TodoRuleMapper;
import com.hahaen.ledger.user.entity.UserNotificationConfig;
import com.hahaen.ledger.user.mapper.UserNotificationConfigMapper;
import com.hahaen.ledger.user.service.NotificationKeyCipher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.time.format.DateTimeFormatter;

@Component @RequiredArgsConstructor
public class TodoReminderWorker {
    private static final Logger log = LoggerFactory.getLogger(TodoReminderWorker.class);
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private final TodoRuleMapper rules;
    private final TodoOccurrenceMapper occurrences;
    private final TodoDeliveryMapper deliveries;
    private final TodoDeliveryAttemptMapper attempts;
    private final UserNotificationConfigMapper configurations;
    private final NotificationKeyCipher cipher;
    private final TodoNotificationSender sender;
    private final TodoMaterializer materializer;

    @Scheduled(fixedDelay = 60000, initialDelay = 15000)
    public void tick() {
        LocalDateTime now = LocalDateTime.now(ZONE);
        for (Long id : rules.dueRuleIds(now.plusDays(1))) {
            try { materializer.generate(id, now.plusDays(1), false); }
            catch (RuntimeException ex) { log.error("待办发生项生成失败，规则ID={}", id, ex); }
        }
        long afterId = 0;
        while (true) {
            List<TodoOccurrence> batch = occurrences.dueForReminder(now, now.minusDays(1), afterId);
            if (batch.isEmpty()) break;
            for (TodoOccurrence occurrence : batch) {
                for (UserNotificationConfig config : activeConfigs(occurrence.getUserId())) {
                    if ("BARK".equals(config.getNotificationType()) || "PUSHPLUS".equals(config.getNotificationType())) {
                        deliveries.ensure(IdWorker.getId(), occurrence.getId(), config.getNotificationType(), now, AuditIdentity.SYSTEM_USER_ID);
                    }
                }
            }
            afterId = batch.get(batch.size() - 1).getId();
            if (batch.size() < 200) break;
        }
        for (TodoDelivery delivery : deliveries.ready(now)) {
            try { sendOne(delivery); }
            catch (RuntimeException ex) {
                // HTTP 异常可能包含含 Key 的请求内容；这里只记录不含敏感字段的类别。
                log.warn("待办通知处理失败，投递ID={}，异常类型={}", delivery.getId(), ex.getClass().getSimpleName());
                deliveries.retry(delivery.getId(), now.plusMinutes(5));
            }
        }
    }

    private void sendOne(TodoDelivery delivery) {
        LocalDateTime now = LocalDateTime.now(ZONE);
        if (deliveries.claim(delivery.getId(), now, now.plusMinutes(2)) != 1) return;
        TodoOccurrence occurrence = occurrences.pending(delivery.getOccurrenceId());
        TodoRule rule = occurrence == null ? null : rules.selectById(occurrence.getRuleId());
        String title = "待办清单提醒";
        String body = "哈记账： " + (occurrence == null ? "待办已结束" : occurrence.getTitle() + "\n计划时间：" + occurrence.getDueAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        TodoDeliveryAttempt attempt = new TodoDeliveryAttempt();
        attempt.setDeliveryId(delivery.getId());
        attempt.setOccurrenceId(delivery.getOccurrenceId());
        attempt.setChannel(delivery.getChannel());
        attempt.setMessageTitle(title);
        attempt.setMessageBody(body);
        attempt.setResult("SENDING");
        attempt.setAttemptedAt(now);
        attempt.setCreatedBy(AuditIdentity.SYSTEM_USER_ID);
        attempt.setCreatedName("系统");
        attempts.insert(attempt);
        if (occurrence == null || rule == null || occurrence.getRemind() != 1 || !"PENDING".equals(occurrence.getStatus())) {
            attempts.finish(attempt.getId(), "SKIPPED");
            deliveries.finish(delivery.getId(), "SKIPPED");
            return;
        }
        UserNotificationConfig config = activeConfigs(occurrence.getUserId()).stream()
                .filter(row -> row.getNotificationType().equals(delivery.getChannel())).findFirst().orElse(null);
        if (config == null) {
            attempts.finish(attempt.getId(), "SKIPPED");
            deliveries.finish(delivery.getId(), "SKIPPED");
            return;
        }
        boolean accepted;
        try { accepted = sender.send(delivery.getChannel(), cipher.decrypt(config.getNotificationKey()), title, body); }
        catch (RuntimeException ex) {
            // 不记录异常消息，以免含有通知 Key。
            accepted = false;
            log.warn("待办通知请求失败，投递ID={}，渠道={}，异常类型={}", delivery.getId(), delivery.getChannel(), ex.getClass().getSimpleName());
        }
        attempts.finish(attempt.getId(), accepted ? "ACCEPTED" : "FAILED");
        if (accepted) deliveries.sent(delivery.getId());
        else if (delivery.getAttempts() >= 4) deliveries.finish(delivery.getId(), "FAILED");
        else deliveries.retry(delivery.getId(), now.plusMinutes(1L << Math.min(delivery.getAttempts(), 4)));
    }
    private List<UserNotificationConfig> activeConfigs(long userId) {
        return configurations.selectList(new LambdaQueryWrapper<UserNotificationConfig>()
                .eq(UserNotificationConfig::getUserId, userId).eq(UserNotificationConfig::getDeleted, 0)
                .in(UserNotificationConfig::getNotificationType, "BARK", "PUSHPLUS"));
    }
}
