package com.hahaen.ledger.calendar.controller;

import com.hahaen.ledger.calendar.service.CalendarService;
import com.hahaen.ledger.calendar.vo.CalendarDayDetailVO;
import com.hahaen.ledger.calendar.vo.CalendarMonthVO;
import com.hahaen.ledger.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/app/calendar")
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService calendarService;

    @GetMapping
    public ApiResponse<CalendarMonthVO> month(@RequestParam int year, @RequestParam int month) {
        return ApiResponse.ok(calendarService.month(year, month));
    }

    @GetMapping("/{date}")
    public ApiResponse<CalendarDayDetailVO> day(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(calendarService.day(date));
    }
}
