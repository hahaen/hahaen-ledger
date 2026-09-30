package com.hahaen.ledger.todo.service;

import com.hahaen.ledger.todo.entity.TodoOccurrence;
import com.hahaen.ledger.common.security.AuditIdentity;
import com.hahaen.ledger.todo.entity.TodoRule;
import com.hahaen.ledger.todo.mapper.TodoOccurrenceMapper;
import com.hahaen.ledger.todo.mapper.TodoRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service @RequiredArgsConstructor
public class TodoMaterializer {
    private final TodoRuleMapper rules;
    private final TodoOccurrenceMapper occurrences;

    @Transactional
    public void generate(long ruleId, LocalDateTime horizon, boolean ensureOne) {
        TodoRule rule = rules.dueForUpdate(ruleId);
        if (rule == null || rule.getNextDueAt() == null) return;
        LocalDateTime next = rule.getNextDueAt();
        for (int i = 0; i < 366 && next != null && (ensureOne && i == 0 || !next.isAfter(horizon)); i++) {
            TodoOccurrence occurrence = new TodoOccurrence();
            occurrence.setRuleId(ruleId);
            occurrence.setUserId(rule.getUserId());
            occurrence.setDueAt(next); occurrence.setAnchorAt(rule.getAnchorAt());
            occurrence.setStatus("PENDING");
            occurrence.setTitle(rule.getTitle()); occurrence.setNote(rule.getNote());
            occurrence.setRecurrence(rule.getRecurrence()); occurrence.setMonthInterval(rule.getMonthInterval());
            occurrence.setRemind(rule.getRemind());
            occurrence.setCreatedBy(AuditIdentity.SYSTEM_USER_ID);
            occurrence.setCreatedName("系统");
            occurrences.insert(occurrence);
            next = TodoSchedule.next(rule, next);
        }
        if (!rule.getNextDueAt().equals(next)) rules.advance(ruleId, next);
    }
}
