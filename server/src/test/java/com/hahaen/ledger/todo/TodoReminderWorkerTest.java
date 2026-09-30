package com.hahaen.ledger.todo;

import com.hahaen.ledger.todo.entity.*;
import com.hahaen.ledger.todo.mapper.*;
import com.hahaen.ledger.todo.service.*;
import com.hahaen.ledger.user.entity.UserNotificationConfig;
import com.hahaen.ledger.user.mapper.UserNotificationConfigMapper;
import com.hahaen.ledger.user.service.NotificationKeyCipher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TodoReminderWorkerTest {
    @ParameterizedTest
    @ValueSource(strings = {"BARK", "PUSHPLUS"})
    void acceptedPushKeepsExactMessageAndChannelInAttempt(String channel) {
        TodoRuleMapper rules = mock(TodoRuleMapper.class);
        TodoOccurrenceMapper occurrences = mock(TodoOccurrenceMapper.class);
        TodoDeliveryMapper deliveries = mock(TodoDeliveryMapper.class);
        TodoDeliveryAttemptMapper attempts = mock(TodoDeliveryAttemptMapper.class);
        UserNotificationConfigMapper configs = mock(UserNotificationConfigMapper.class);
        NotificationKeyCipher cipher = mock(NotificationKeyCipher.class);
        TodoNotificationSender sender = mock(TodoNotificationSender.class);
        TodoMaterializer materializer = mock(TodoMaterializer.class);
        TodoReminderWorker worker = new TodoReminderWorker(rules, occurrences, deliveries, attempts, configs, cipher, sender, materializer);
        TodoDelivery delivery = new TodoDelivery();
        delivery.setId(12L); delivery.setOccurrenceId(21L); delivery.setChannel(channel); delivery.setAttempts(0);
        TodoOccurrence occurrence = new TodoOccurrence();
        occurrence.setId(21L); occurrence.setRuleId(31L); occurrence.setUserId(41L);
        occurrence.setTitle("给植物浇水"); occurrence.setRemind(1); occurrence.setStatus("PENDING");
        occurrence.setDueAt(LocalDateTime.parse("2026-09-30T09:00"));
        TodoRule rule = new TodoRule(); rule.setId(31L);
        UserNotificationConfig config = new UserNotificationConfig();
        config.setNotificationType(channel); config.setNotificationKey("encrypted");
        when(rules.dueRuleIds(any())).thenReturn(List.of());
        when(occurrences.dueForReminder(any(),any(),anyLong())).thenReturn(List.of());
        when(deliveries.ready(any())).thenReturn(List.of(delivery));
        when(deliveries.claim(eq(12L),any(),any())).thenReturn(1);
        when(occurrences.pending(21L)).thenReturn(occurrence);
        when(rules.selectById(31L)).thenReturn(rule);
        when(configs.selectList(any())).thenReturn(List.of(config));
        when(cipher.decrypt("encrypted")).thenReturn("synthetic-key");
        when(sender.send(eq(channel),eq("synthetic-key"),eq("待办清单提醒"),anyString())).thenReturn(true);
        when(attempts.insert(any(TodoDeliveryAttempt.class))).thenAnswer(invocation -> { ((TodoDeliveryAttempt) invocation.getArgument(0)).setId(51L); return 1; });
        worker.tick();
        var saved = org.mockito.ArgumentCaptor.forClass(TodoDeliveryAttempt.class);
        verify(attempts).insert(saved.capture());
        assertEquals(channel, saved.getValue().getChannel());
        assertEquals("待办清单提醒", saved.getValue().getMessageTitle());
        assertEquals("哈记账： 给植物浇水\n计划时间：2026-09-30 09:00", saved.getValue().getMessageBody());
        verify(sender).send(channel, "synthetic-key", "待办清单提醒", saved.getValue().getMessageBody());
        verify(attempts).finish(51L, "ACCEPTED");
        verify(deliveries).sent(12L);
    }
}
