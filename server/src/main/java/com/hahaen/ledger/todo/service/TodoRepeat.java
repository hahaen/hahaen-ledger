package com.hahaen.ledger.todo.service;

import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.todo.entity.TodoRule;
import com.hahaen.ledger.todo.entity.TodoOccurrence;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/** 自定义公历规则：选择值以有界CSV保存，历史发生项保留完整快照。 */
public final class TodoRepeat {
    private TodoRepeat() {}
    public static boolean afterCompletion(TodoRule rule) {
        return "CUSTOM".equals(rule.getRecurrence()) && "AFTER_COMPLETION".equals(rule.getRepeatMode());
    }
    public static Set<Integer> numbers(String csv, int max) {
        TreeSet<Integer> result = new TreeSet<>();
        if (csv == null || csv.isEmpty()) return result;
        if (csv.length() > 100) invalid();
        for (String part : csv.split(",", -1)) {
            if (!part.matches("[1-9][0-9]?")) invalid();
            int value = Integer.parseInt(part);
            if (value > max || !result.add(value)) invalid();
        }
        return result;
    }
    private static Set<LocalDate> dates(String csv, boolean annual) {
        TreeSet<LocalDate> result = new TreeSet<>();
        if (csv == null || csv.isEmpty()) return result;
        if (csv.length() > (annual ? 2200 : 1100)) invalid();
        try {
            for (String part : csv.split(",", -1)) {
                if (!part.matches(annual ? "\\d{2}-\\d{2}" : "\\d{4}-\\d{2}-\\d{2}")) invalid();
                LocalDate date = LocalDate.parse(annual ? "2000-" + part : part);
                if (!annual && (date.getYear() < 2000 || date.getYear() > 2099)) invalid();
                if (!result.add(date)) invalid();
            }
        } catch (DateTimeException ex) { invalid(); }
        if (result.size() > (annual ? 366 : 100)) invalid();
        return result;
    }
    public static void validate(TodoRule rule) {
        if (!"CUSTOM".equals(rule.getRecurrence())) return;
        if (rule.getRepeatMode() == null || !Set.of("TIME", "AFTER_COMPLETION", "FIXED_DATES").contains(rule.getRepeatMode())) invalid();
        if ("FIXED_DATES".equals(rule.getRepeatMode())) {
            Set<LocalDate> selected = dates(rule.getFixedDates(), false);
            if (selected.isEmpty()) invalid();
            if (rule.getRepeatUnit() != null || rule.getRepeatInterval() != null || hasSelections(rule)) invalid();
            return;
        }
        if (rule.getRepeatUnit() == null || !Set.of("DAY", "WEEK", "MONTH", "YEAR").contains(rule.getRepeatUnit())
                || rule.getRepeatInterval() == null || rule.getRepeatInterval() < 1 || rule.getRepeatInterval() > 365
                || present(rule.getFixedDates())) invalid();
        if (afterCompletion(rule)) { if (hasSelections(rule)) invalid(); return; }
        Set<Integer> week = numbers(rule.getWeekDays(), 7), month = numbers(rule.getMonthDays(), 31);
        Set<LocalDate> year = dates(rule.getYearDays(), true);
        if ("WEEK".equals(rule.getRepeatUnit()) ? week.isEmpty() : !week.isEmpty()) invalid();
        if ("MONTH".equals(rule.getRepeatUnit()) ? month.isEmpty() && !lastDay(rule) : !month.isEmpty() || lastDay(rule)) invalid();
        if ("YEAR".equals(rule.getRepeatUnit()) ? year.isEmpty() : !year.isEmpty()) invalid();
    }
    private static boolean hasSelections(TodoRule r) {
        return present(r.getWeekDays()) || present(r.getMonthDays()) || present(r.getYearDays()) || lastDay(r);
    }
    private static boolean present(String s) { return s != null && !s.isEmpty(); }
    private static boolean lastDay(TodoRule r) { return Integer.valueOf(1).equals(r.getLastDay()); }
    private static void invalid() { throw new BusinessException("TODO_REPEAT_INVALID", "请选择有效的重复规则和日期"); }

    /** 不早于所选计划时间的第一个匹配时间。 */
    public static LocalDateTime first(TodoRule rule, LocalDateTime from) {
        if (!"CUSTOM".equals(rule.getRecurrence()) || afterCompletion(rule)) return from;
        return scheduled(rule, from, true);
    }
    public static LocalDateTime scheduled(TodoRule rule, LocalDateTime from, boolean inclusive) {
        LocalDate anchor = rule.getAnchorAt().toLocalDate();
        LocalTime time = rule.getAnchorAt().toLocalTime();
        if ("FIXED_DATES".equals(rule.getRepeatMode())) {
            for (LocalDate date : dates(rule.getFixedDates(), false)) {
                LocalDateTime candidate = date.atTime(time);
                if (candidate.isAfter(from) || inclusive && candidate.equals(from)) return candidate;
            }
            return null;
        }
        Set<Integer> week = numbers(rule.getWeekDays(), 7), month = numbers(rule.getMonthDays(), 31);
        Set<LocalDate> year = dates(rule.getYearDays(), true);
        int interval = rule.getRepeatInterval();
        LocalDate start = from.toLocalDate().isBefore(anchor) ? anchor : from.toLocalDate();
        for (LocalDate date = start; date.getYear() <= 2099; date = date.plusDays(1)) {
            LocalDateTime candidate = date.atTime(time);
            if (candidate.isBefore(from) || !inclusive && candidate.equals(from)) continue;
            boolean match = switch (rule.getRepeatUnit()) {
                case "DAY" -> ChronoUnit.DAYS.between(anchor, date) % interval == 0;
                case "WEEK" -> ChronoUnit.WEEKS.between(anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))) % interval == 0
                        && week.contains(date.getDayOfWeek().getValue());
                case "MONTH" -> ChronoUnit.MONTHS.between(YearMonth.from(anchor), YearMonth.from(date)) % interval == 0
                        && (lastDay(rule) && date.getDayOfMonth() == date.lengthOfMonth()
                        || month.contains(date.getDayOfMonth())
                        || date.getDayOfMonth() == date.lengthOfMonth() && month.stream().anyMatch(d -> d > candidate.toLocalDate().lengthOfMonth()));
                case "YEAR" -> (date.getYear() - anchor.getYear()) % interval == 0
                        && year.stream().anyMatch(d -> d.getMonthValue() == candidate.getMonthValue()
                        && Math.min(d.getDayOfMonth(), candidate.toLocalDate().lengthOfMonth()) == candidate.getDayOfMonth());
                default -> throw new BusinessException("TODO_REPEAT_INVALID", "重复单位无效");
            };
            if (match) return candidate;
        }
        return null;
    }
    public static LocalDateTime completedNext(TodoRule rule, LocalDateTime completedAt) {
        int interval = rule.getRepeatInterval();
        LocalDateTime next = switch (rule.getRepeatUnit()) {
            case "DAY" -> completedAt.plusDays(interval);
            case "WEEK" -> completedAt.plusWeeks(interval);
            case "MONTH" -> completedAt.plusMonths(interval);
            case "YEAR" -> completedAt.plusYears(interval);
            default -> throw new BusinessException("TODO_REPEAT_INVALID", "重复单位无效");
        };
        return next.getYear() > 2099 ? null : next;
    }
    public static void snapshot(TodoRule rule, TodoOccurrence row) {
        row.setRepeatMode(rule.getRepeatMode()); row.setRepeatUnit(rule.getRepeatUnit()); row.setRepeatInterval(rule.getRepeatInterval());
        row.setWeekDays(rule.getWeekDays()); row.setMonthDays(rule.getMonthDays()); row.setLastDay(rule.getLastDay());
        row.setYearDays(rule.getYearDays()); row.setFixedDates(rule.getFixedDates());
    }
}
