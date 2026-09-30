package com.hahaen.ledger.todo.service;

import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.todo.entity.TodoRule;
import java.time.LocalDateTime;
import java.time.YearMonth;

/** 北京时间的墙上时间规则；缺少的月日落在该月最后一天。 */
public final class TodoSchedule {
    private TodoSchedule() {}
    public static LocalDateTime next(TodoRule rule, LocalDateTime current) {
        LocalDateTime anchor = rule.getAnchorAt();
        LocalDateTime result = switch (rule.getRecurrence()) {
            case "ONCE" -> null;
            case "CUSTOM" -> TodoRepeat.afterCompletion(rule) ? null : TodoRepeat.scheduled(rule, current, false);
            case "DAILY" -> current.plusDays(1);
            case "MONTHLY", "EVERY_N_MONTHS" -> {
                int interval = rule.getRecurrence().equals("MONTHLY") ? 1 : rule.getMonthInterval();
                YearMonth month = YearMonth.from(current).plusMonths(interval);
                yield LocalDateTime.of(month.atDay(Math.min(anchor.getDayOfMonth(), month.lengthOfMonth())), anchor.toLocalTime());
            }
            case "YEARLY" -> {
                YearMonth month = YearMonth.of(current.getYear() + 1, anchor.getMonth());
                yield LocalDateTime.of(month.atDay(Math.min(anchor.getDayOfMonth(), month.lengthOfMonth())), anchor.toLocalTime());
            }
            default -> throw new BusinessException("TODO_RECURRENCE_INVALID", "重复规则无效");
        };
        return result != null && result.getYear() > 2099 ? null : result;
    }
}
