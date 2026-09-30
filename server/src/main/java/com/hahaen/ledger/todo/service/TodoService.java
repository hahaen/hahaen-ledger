package com.hahaen.ledger.todo.service;

import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.CurrentUser;
import com.hahaen.ledger.todo.dto.TodoSaveRequest;
import com.hahaen.ledger.todo.entity.TodoOccurrence;
import com.hahaen.ledger.todo.entity.TodoRule;
import com.hahaen.ledger.todo.mapper.TodoOccurrenceMapper;
import com.hahaen.ledger.todo.mapper.TodoDeliveryAttemptMapper;
import com.hahaen.ledger.todo.vo.TodoAttemptVO;
import com.hahaen.ledger.todo.mapper.TodoRuleMapper;
import com.hahaen.ledger.todo.vo.TodoItemVO;
import com.hahaen.ledger.todo.vo.TodoPageVO;
import com.hahaen.ledger.user.entity.UserNotificationConfig;
import com.hahaen.ledger.user.mapper.UserNotificationConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service @RequiredArgsConstructor
public class TodoService {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final Pattern KEY = Pattern.compile("[A-Za-z0-9_-]{8,64}");
    private static final Set<String> RECURRENCES = Set.of("ONCE", "DAILY", "MONTHLY", "EVERY_N_MONTHS", "YEARLY", "CUSTOM");
    private final TodoRuleMapper rules;
    private final TodoOccurrenceMapper occurrences;
    private final UserNotificationConfigMapper configurations;
    private final TodoMaterializer materializer;
    private final TodoDeliveryAttemptMapper attempts;

    public TodoPageVO list(String status, int page, int pageSize) {
        long userId = CurrentUser.id();
        if (rules.activeUser(userId) == null) throw new BusinessException("USER_NOT_FOUND", "用户不存在或已停用");
        if (!Set.of("PENDING", "COMPLETED").contains(status) || page < 1 || pageSize < 1 || pageSize > 50 || page > 100000) {
            throw new BusinessException("TODO_QUERY_INVALID", "待办查询参数无效");
        }
        int offset = (page - 1) * pageSize;
        List<TodoOccurrence> rows = occurrences.list(userId, status, pageSize + 1, offset);
        boolean hasMore = rows.size() > pageSize;
        return new TodoPageVO(occurrences.count(userId, "PENDING"), occurrences.count(userId, "COMPLETED"),
                rows.stream().limit(pageSize).map(this::toVO).toList(), hasMore);
    }

    public TodoItemVO detail(long occurrenceId) {
        long userId = CurrentUser.id();
        if (rules.activeUser(userId) == null) throw new BusinessException("USER_NOT_FOUND", "用户不存在或已停用");
        TodoOccurrence occurrence = occurrences.pendingOwned(userId, occurrenceId);
        if (occurrence == null) throw new BusinessException("TODO_NOT_FOUND", "未完成待办不存在，请刷新");
        return toVO(occurrence);
    }

    public List<TodoAttemptVO> attempts(long occurrenceId) {
        long userId = CurrentUser.id();
        TodoOccurrence occurrence = occurrences.selectById(occurrenceId);
        if (occurrence == null || occurrence.getUserId() != userId)
            throw new BusinessException("TODO_NOT_FOUND", "待办不存在");
        return attempts.listOwned(userId, occurrenceId).stream()
                .map(row -> new TodoAttemptVO(row.getChannel(), row.getMessageTitle(), row.getMessageBody(),
                        row.getResult(), row.getAttemptedAt())).toList();
    }

    @Transactional
    public String create(TodoSaveRequest request) {
        long userId = lockUser();
        validateKey(request.idempotencyKey());
        String hash = hash("CREATE|" + canonical(request));
        Long replay = replay(userId, request.idempotencyKey(), hash);
        if (replay != null) return replay.toString();
        validate(request, userId);
        TodoRule rule = new TodoRule();
        rule.setUserId(userId);
        populate(rule, request);
        LocalDateTime firstDue = TodoRepeat.first(rule, request.dueAt());
        if (firstDue == null) throw new BusinessException("TODO_DATE_INVALID", "重复规则在2099年前没有可用日期");
        rule.setNextDueAt(TodoSchedule.next(rule, firstDue));
        rule.setActive(1);
        rules.insert(rule);
        TodoOccurrence first = occurrence(rule, firstDue);
        occurrences.insert(first);
        rules.saveRequest(userId, request.idempotencyKey(), hash, rule.getId());
        return rule.getId().toString();
    }

    @Transactional
    public void edit(long occurrenceId, TodoSaveRequest request) {
        long userId = lockUser();
        validateKey(request.idempotencyKey());
        String hash = hash("EDIT|" + occurrenceId + "|" + canonical(request));
        if (replay(userId, request.idempotencyKey(), hash) != null) return;
        validate(request, userId);
        TodoOccurrence occurrence = pendingOwned(userId, occurrenceId);
        TodoRule rule = rules.ownedForUpdate(userId, occurrence.getRuleId());
        if (rule == null || rule.getActive() != 1) throw new BusinessException("TODO_NOT_FOUND", "待办规则不存在");
        LocalDateTime lastCompletedDue = occurrences.lastCompletedDue(rule.getId(), userId);
        boolean scheduleUnchanged = request.dueAt().equals(occurrence.getDueAt())
                && request.recurrence().equals(rule.getRecurrence())
                && java.util.Objects.equals(request.monthInterval() == null ? 1 : request.monthInterval(), rule.getMonthInterval())
                && sameRepeat(rule, request);
        LocalDateTime originalAnchor = rule.getAnchorAt();
        // 编辑仅替换未完成项，完成历史不变；旧投递随软删除发生项停止重试。
        occurrences.deletePendingRule(rule.getId(), userId, CurrentUser.optionalName());
        populate(rule, request);
        if (scheduleUnchanged) rule.setAnchorAt(originalAnchor);
        LocalDateTime firstDue = TodoRepeat.first(rule, request.dueAt());
        if (firstDue == null) throw new BusinessException("TODO_DATE_INVALID", "重复规则在2099年前没有可用日期");
        if (lastCompletedDue != null && !firstDue.isAfter(lastCompletedDue)) {
            firstDue = TodoSchedule.next(rule, lastCompletedDue);
            if (firstDue == null) throw new BusinessException("TODO_DATE_INVALID", "单次计划时间必须晚于已完成记录");
        }
        rule.setUpdateName(CurrentUser.optionalName());
        rule.setNextDueAt(TodoSchedule.next(rule, firstDue));
        rules.edit(rule);
        occurrences.insert(occurrence(rule, firstDue));
        rules.saveRequest(userId, request.idempotencyKey(), hash, rule.getId());
    }

    @Transactional
    public void complete(long occurrenceId, String key) {
        long userId = lockUser();
        String hash = hash("COMPLETE|" + occurrenceId);
        if (replay(userId, key, hash) != null) return;
        TodoOccurrence occurrence = pendingOwned(userId, occurrenceId);
        if (occurrences.complete(occurrenceId, userId, CurrentUser.optionalName(), now()) != 1)
            throw new BusinessException("TODO_CHANGED", "待办状态已变化，请刷新");
        rules.saveRequest(userId, key, hash, occurrenceId);
        TodoRule rule = rules.ownedForUpdate(userId, occurrence.getRuleId());
        if (rule != null && rule.getActive() == 1 && TodoRepeat.afterCompletion(rule)) {
            LocalDateTime completedAt = occurrences.selectById(occurrenceId).getCompletedAt();
            rules.advance(rule.getId(), TodoRepeat.completedNext(rule, completedAt));
        }
        materializer.generate(occurrence.getRuleId(), now().plusDays(1), true);
    }

    @Transactional
    public void delete(long occurrenceId, String key) {
        long userId = lockUser();
        String hash = hash("DELETE|" + occurrenceId);
        if (replay(userId, key, hash) != null) return;
        TodoOccurrence occurrence = occurrences.ownedForUpdate(userId, occurrenceId);
        if (occurrence == null) throw new BusinessException("TODO_NOT_FOUND", "待办不存在");
        String name = CurrentUser.optionalName();
        if ("PENDING".equals(occurrence.getStatus())) {
            // 删除未完成重复待办结束整条规则，避免第二天意外再出现。
            TodoRule rule = rules.ownedForUpdate(userId, occurrence.getRuleId());
            if (rule == null) throw new BusinessException("TODO_NOT_FOUND", "待办规则不存在");
            rules.end(rule.getId(), userId, name);
            occurrences.deletePendingRule(rule.getId(), userId, name);
        } else {
            occurrences.deleteOne(occurrenceId, userId, name);
        }
        rules.saveRequest(userId, key, hash, occurrenceId);
    }

    private TodoItemVO toVO(TodoOccurrence occurrence) {
        return new TodoItemVO(occurrence.getId().toString(), occurrence.getRuleId().toString(), occurrence.getTitle(), occurrence.getNote(),
                occurrence.getRecurrence(), occurrence.getMonthInterval(), occurrence.getDueAt(), occurrence.getAnchorAt(), occurrence.getRemind() == 1,
                occurrence.getStatus(), occurrence.getCompletedAt(), occurrence.getRepeatMode(), occurrence.getRepeatUnit(),
                occurrence.getRepeatInterval(), occurrence.getWeekDays(), occurrence.getMonthDays(),
                Integer.valueOf(1).equals(occurrence.getLastDay()), occurrence.getYearDays(), occurrence.getFixedDates());
    }

    private long lockUser() {
        long id = CurrentUser.id();
        if (rules.lockUser(id) == null) throw new BusinessException("USER_NOT_FOUND", "用户不存在或已停用");
        return id;
    }
    private TodoOccurrence pendingOwned(long userId, long id) {
        TodoOccurrence value = occurrences.ownedForUpdate(userId, id);
        if (value == null || !"PENDING".equals(value.getStatus()))
            throw new BusinessException("TODO_NOT_FOUND", "未完成待办不存在，请刷新");
        return value;
    }
    private void validate(TodoSaveRequest value, long userId) {
        validateKey(value.idempotencyKey());
        if (value.title() == null || value.title().trim().isEmpty() || value.title().trim().length() > 100
                || value.note() != null && value.note().length() > 500)
            throw new BusinessException("TODO_TEXT_INVALID", "标题或备注长度无效");
        if (value.recurrence() == null || !RECURRENCES.contains(value.recurrence())) throw new BusinessException("TODO_RECURRENCE_INVALID", "重复规则无效");
        int interval = value.monthInterval() == null ? 1 : value.monthInterval();
        if ("EVERY_N_MONTHS".equals(value.recurrence()) ? interval < 2 || interval > 120 : interval != 1)
            throw new BusinessException("TODO_INTERVAL_INVALID", "间隔月数无效");
        if (value.dueAt() == null || !value.dueAt().isAfter(now()) || value.dueAt().getYear() > 2099)
            throw new BusinessException("TODO_DATE_INVALID", "计划时间必须晚于现在且不晚于2099年");
        TodoRule candidate = new TodoRule();
        populate(candidate, value);
        TodoRepeat.validate(candidate);
        if (value.remind() && configurations.selectCount(new LambdaQueryWrapper<UserNotificationConfig>()
                .eq(UserNotificationConfig::getUserId, userId).eq(UserNotificationConfig::getDeleted, 0)
                .in(UserNotificationConfig::getNotificationType, "BARK", "PUSHPLUS")) == 0)
            throw new BusinessException("TODO_NOTIFICATION_UNCONFIGURED", "请先在通知中心配置 Bark 或 pushplus");
    }
    private void populate(TodoRule rule, TodoSaveRequest value) {
        rule.setTitle(value.title().trim());
        rule.setNote(value.note() == null || value.note().isBlank() ? null : value.note().trim());
        rule.setRecurrence(value.recurrence());
        rule.setMonthInterval(value.monthInterval() == null ? 1 : value.monthInterval());
        rule.setAnchorAt(value.dueAt());
        rule.setRemind(value.remind() ? 1 : 0);
        boolean custom = "CUSTOM".equals(value.recurrence());
        rule.setRepeatMode(custom ? value.repeatMode() : null); rule.setRepeatUnit(custom ? value.repeatUnit() : null);
        rule.setRepeatInterval(custom ? value.repeatInterval() : null); rule.setWeekDays(custom ? value.weekDays() : null);
        rule.setMonthDays(custom ? value.monthDays() : null); rule.setLastDay(custom && value.lastDay() ? 1 : 0);
        rule.setYearDays(custom ? value.yearDays() : null); rule.setFixedDates(custom ? value.fixedDates() : null);
    }
    private TodoOccurrence occurrence(TodoRule rule, LocalDateTime dueAt) {
        TodoOccurrence row = new TodoOccurrence();
        row.setRuleId(rule.getId()); row.setUserId(rule.getUserId()); row.setDueAt(dueAt); row.setAnchorAt(rule.getAnchorAt()); row.setStatus("PENDING");
        row.setTitle(rule.getTitle()); row.setNote(rule.getNote()); row.setRecurrence(rule.getRecurrence());
        row.setMonthInterval(rule.getMonthInterval()); row.setRemind(rule.getRemind());
        TodoRepeat.snapshot(rule, row);
        return row;
    }
    private boolean sameRepeat(TodoRule rule, TodoSaveRequest value) {
        if (!"CUSTOM".equals(value.recurrence())) return true;
        return java.util.Objects.equals(rule.getRepeatMode(), value.repeatMode())
                && java.util.Objects.equals(rule.getRepeatUnit(), value.repeatUnit())
                && java.util.Objects.equals(rule.getRepeatInterval(), value.repeatInterval())
                && java.util.Objects.equals(rule.getWeekDays(), value.weekDays())
                && java.util.Objects.equals(rule.getMonthDays(), value.monthDays())
                && java.util.Objects.equals(rule.getYearDays(), value.yearDays())
                && java.util.Objects.equals(rule.getFixedDates(), value.fixedDates())
                && Integer.valueOf(1).equals(rule.getLastDay()) == value.lastDay();
    }
    private Long replay(long userId, String key, String hash) {
        validateKey(key);
        String previous = rules.requestHash(userId, key);
        if (previous == null) return null;
        if (!previous.equals(hash)) throw new BusinessException("IDEMPOTENCY_CONFLICT", "幂等键已用于其他请求");
        return rules.requestResult(userId, key);
    }
    private static void validateKey(String key) {
        if (key == null || !KEY.matcher(key).matches()) throw new BusinessException("IDEMPOTENCY_KEY_INVALID", "幂等键无效");
    }
    private static String canonical(TodoSaveRequest request) {
        return request.title().trim() + "\n" + request.note() + "\n" + request.recurrence() + "\n"
                + request.monthInterval() + "\n" + request.dueAt() + "\n" + request.remind()
                + ("CUSTOM".equals(request.recurrence()) ? "\n" + request.repeatMode() + "|" + request.repeatUnit()
                + "|" + request.repeatInterval() + "|" + request.weekDays() + "|" + request.monthDays()
                + "|" + request.lastDay() + "|" + request.yearDays() + "|" + request.fixedDates() : "");
    }
    private static String hash(String input) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException ex) { throw new IllegalStateException("SHA-256不可用", ex); }
    }
    private static LocalDateTime now() { return LocalDateTime.now(ZONE); }
}
