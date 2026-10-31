package com.hahaen.ledger.todo.service;

import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.todo.dto.TodoPlanRequest;
import com.hahaen.ledger.todo.entity.TodoOccurrence;
import com.hahaen.ledger.todo.entity.TodoRule;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/** 写入与预览共用同一套规则校验、编辑判断和首项计算。 */
public final class TodoPlanning {
    private TodoPlanning() {}
    public static TodoRule rule(TodoPlanRequest value) {
        if (value == null || value.recurrence() == null || !Set.of("ONCE", "DAILY", "MONTHLY", "EVERY_N_MONTHS", "YEARLY", "CUSTOM").contains(value.recurrence()))
            throw new BusinessException("TODO_RECURRENCE_INVALID", "重复规则无效");
        int interval = value.monthInterval() == null ? 1 : value.monthInterval();
        if ("EVERY_N_MONTHS".equals(value.recurrence()) ? interval < 2 || interval > 120 : interval != 1)
            throw new BusinessException("TODO_INTERVAL_INVALID", "间隔月数无效");
        if (value.dueAt() == null || value.dueAt().getYear() < 2000 || value.dueAt().getYear() > 2099)
            throw new BusinessException("TODO_DATE_INVALID", "请选择2000至2099年的有效时间");
        TodoRule rule = new TodoRule();
        rule.setRecurrence(value.recurrence()); rule.setMonthInterval(interval); rule.setAnchorAt(value.dueAt());
        boolean custom = "CUSTOM".equals(value.recurrence());
        rule.setRepeatMode(custom ? value.repeatMode() : null); rule.setRepeatUnit(custom ? value.repeatUnit() : null);
        rule.setRepeatInterval(custom ? value.repeatInterval() : null); rule.setWeekDays(custom ? empty(value.weekDays()) : null);
        rule.setMonthDays(custom ? empty(value.monthDays()) : null); rule.setLastDay(custom && value.lastDay() ? 1 : 0);
        rule.setYearDays(custom ? empty(value.yearDays()) : null); rule.setFixedDates(custom ? empty(value.fixedDates()) : null);
        TodoRepeat.validate(rule);
        return rule;
    }
    public static boolean unchanged(TodoRule original, TodoOccurrence occurrence, TodoRule candidate) {
        return (candidate.getAnchorAt().equals(occurrence.getDueAt()) || candidate.getAnchorAt().equals(original.getAnchorAt()))
                && Objects.equals(original.getRecurrence(), candidate.getRecurrence())
                && Objects.equals(original.getMonthInterval(), candidate.getMonthInterval())
                && Objects.equals(original.getRepeatMode(), candidate.getRepeatMode())
                && Objects.equals(original.getRepeatUnit(), candidate.getRepeatUnit())
                && Objects.equals(original.getRepeatInterval(), candidate.getRepeatInterval())
                && Objects.equals(empty(original.getWeekDays()), candidate.getWeekDays())
                && Objects.equals(empty(original.getMonthDays()), candidate.getMonthDays())
                && Objects.equals(empty(original.getYearDays()), candidate.getYearDays())
                && Objects.equals(empty(original.getFixedDates()), candidate.getFixedDates())
                && Integer.valueOf(1).equals(original.getLastDay()) == Integer.valueOf(1).equals(candidate.getLastDay());
    }
    public static LocalDateTime first(TodoRule candidate, LocalDateTime lastCompletedDue, LocalDateTime now) {
        LocalDateTime from = candidate.getAnchorAt();
        boolean calendar = "CUSTOM".equals(candidate.getRecurrence()) && "TIME".equals(candidate.getRepeatMode());
        if (calendar ? from.toLocalDate().isBefore(now.toLocalDate()) : !from.isAfter(now))
            throw new BusinessException("TODO_DATE_INVALID", calendar ? "开始日期不能早于今天" : "计划时间必须晚于现在");
        // 日期与执行时刻拆分后，今天已过去的时刻不补生成；周期仍以用户选择的日期为锚点。
        LocalDateTime first = TodoRepeat.first(candidate, calendar && !from.isAfter(now) ? now.plusNanos(1) : from);
        if (first != null && lastCompletedDue != null && !first.isAfter(lastCompletedDue)) first = TodoSchedule.next(candidate, lastCompletedDue);
        if (first == null) throw new BusinessException("TODO_DATE_INVALID", "重复规则在2099年前没有可用日期");
        return first;
    }
    private static String empty(String value) { return value == null || value.isEmpty() ? null : value; }
}
