package com.hahaen.ledger.user.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfileServiceTest {
    @Test
    void countsCreationDayAsTheFirstCumulativeDay() {
        LocalDate today = LocalDate.of(2026, 9, 6);

        assertEquals(1, ProfileService.calculateCumulativeDays(today, today));
        assertEquals(2, ProfileService.calculateCumulativeDays(today.minusDays(1), today));
        assertEquals(1, ProfileService.calculateCumulativeDays(today.plusDays(1), today));
    }
}
