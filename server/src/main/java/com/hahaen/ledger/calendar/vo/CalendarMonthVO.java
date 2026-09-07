package com.hahaen.ledger.calendar.vo;

import java.util.List;

public record CalendarMonthVO(String month, List<CalendarDayVO> days) {
}
