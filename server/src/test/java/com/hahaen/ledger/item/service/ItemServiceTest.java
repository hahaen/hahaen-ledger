package com.hahaen.ledger.item.service;

import com.hahaen.ledger.item.dto.*;
import com.hahaen.ledger.item.entity.PersonalItem;
import com.hahaen.ledger.item.mapper.PersonalItemMapper;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ItemServiceTest {
    private static final String KEY = "item_test_1234567890";
    private final PersonalItemMapper mapper = mock(PersonalItemMapper.class);
    private final ItemService service = new ItemService(mapper);
    private final LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
    private MockedStatic<CurrentUser> identity() {
        var identity = mockStatic(CurrentUser.class);
        identity.when(CurrentUser::id).thenReturn(7L);
        identity.when(CurrentUser::optionalId).thenReturn(7L);
        identity.when(CurrentUser::optionalName).thenReturn("测试用户");
        when(mapper.lockUser(7L)).thenReturn(7L);
        return identity;
    }
    private PersonalItem item(LocalDate purchased) {
        PersonalItem item = new PersonalItem(); item.setId(11L); item.setUserId(7L);
        item.setName("耳机"); item.setPriceCent(10000L); item.setPurchasedOn(purchased);
        item.setStatus("ACTIVE"); item.setDeleted(0); return item;
    }
    @Test void purchaseDayCountsAsOneEvenAtZeroPrice() {
        var item = item(today); item.setPriceCent(0L);
        assertEquals(1, ItemCosts.view(item, today).serviceDays());
        assertEquals(0, ItemCosts.view(item, today).dailyCostCents().compareTo(BigDecimal.ZERO));
        assertEquals(1, ItemCosts.history(item, today).size());
    }
    @Test void leapYearAndInclusiveDaysAreCorrect() {
        var item = item(LocalDate.of(2024, 2, 28));
        var view = ItemCosts.view(item, LocalDate.of(2024, 3, 1));
        assertEquals(3, view.serviceDays());
        assertEquals(new BigDecimal("3333.33333333"), view.dailyCostCents());
    }
    @Test void retirementFreezesDaysAndAllowsResaleProfit() {
        var item = item(today.minusDays(100)); item.setStatus("RETIRED");
        item.setRetiredOn(today.minusDays(91)); item.setResaleCent(12000L);
        var view = ItemCosts.view(item, today);
        assertEquals(10, view.serviceDays()); assertEquals(-2000, view.netCostCents());
        assertEquals(new BigDecimal("-200.00000000"), view.dailyCostCents());
        var points = ItemCosts.history(item, today);
        assertEquals(view.dailyCostCents(), points.getLast().dailyCostCents());
        assertEquals(item.getRetiredOn(), points.getLast().date());
        assertEquals(new BigDecimal("10000.00000000"), points.getFirst().dailyCostCents());
    }
    @Test void longHistoryIsBoundedAndContainsBothEndpoints() {
        var item = item(today.minusDays(10000)); var points = ItemCosts.history(item, today);
        assertEquals(31, points.size()); assertEquals(1, points.getFirst().day());
        assertEquals(10001, points.getLast().day());
        assertEquals(today, points.getLast().date());
    }
    @Test void createRetryReusesRecordAndRejectsChangedPayload() {
        try (var ignored = identity()) {
            doAnswer(call -> { PersonalItem value = call.getArgument(0); value.setId(11L); value.setDeleted(0);
                when(mapper.byCreateKey(7L, KEY)).thenReturn(value); return 1; }).when(mapper).insert(any(PersonalItem.class));
            var request = new ItemRequest(" 耳机 ", 10000L, today, true, null, null, KEY);
            var first = service.create(request); assertEquals(first.id(), service.create(request).id());
            assertThrows(BusinessException.class, () -> service.create(new ItemRequest("耳机", 10100L, today, true, null, null, KEY)));
            verify(mapper, times(1)).insert(any(PersonalItem.class));
        }
    }
    @Test void createRetryAfterRetirementUsesOriginalFingerprint() {
        try (var ignored = identity()) {
            doAnswer(call -> { PersonalItem value = call.getArgument(0); value.setId(11L); value.setDeleted(0);
                when(mapper.byCreateKey(7L, KEY)).thenReturn(value); when(mapper.ownedForUpdate(7L, 11L)).thenReturn(value); return 1; }).when(mapper).insert(any(PersonalItem.class));
            var request = new ItemRequest("耳机", 10000L, today, true, null, null, KEY);
            service.create(request); service.retire(11L, new RetireItemRequest(today, 2000L, "retire_test_1234567890"));
            assertEquals("RETIRED", service.create(request).status()); verify(mapper, times(1)).insert(any(PersonalItem.class));
        }
    }
    @Test void rejectsFutureDateAndInvalidRetirementWithoutWriting() {
        try (var ignored = identity()) {
            assertThrows(BusinessException.class, () -> service.create(new ItemRequest("耳机", 10000L, today.plusDays(1), true, null, null, KEY)));
            assertThrows(BusinessException.class, () -> service.create(new ItemRequest("耳机", 10000L, today, false, today.minusDays(1), 0L, KEY)));
            assertThrows(BusinessException.class, () -> service.create(new ItemRequest("耳机", -1L, today, true, null, null, KEY)));
            assertThrows(BusinessException.class, () -> service.create(new ItemRequest("耳机", 1L, today, true, today, 0L, KEY)));
            verify(mapper, never()).insert(any(PersonalItem.class));
        }
    }
    @Test void retireRetryWritesOnceAndConflictPreservesOriginalResale() {
        try (var ignored = identity()) {
            var item = item(today.minusDays(9)); when(mapper.ownedForUpdate(7L, 11L)).thenReturn(item);
            var request = new RetireItemRequest(today, 2000L, KEY);
            service.retire(11L, request); service.retire(11L, request);
            assertThrows(BusinessException.class, () -> service.retire(11L, new RetireItemRequest(today, 3000L, KEY)));
            assertEquals(2000L, item.getResaleCent()); verify(mapper, times(1)).updateById(item);
        }
    }
    @Test void foreignOrDeletedItemsCannotBeReadRetiredOrDeleted() {
        try (var ignored = identity()) {
            assertThrows(BusinessException.class, () -> service.detail(99L));
            assertThrows(BusinessException.class, () -> service.retire(99L, new RetireItemRequest(today, 0L, KEY)));
            assertThrows(BusinessException.class, () -> service.delete(99L, KEY));
            var deleted = item(today); deleted.setDeleted(1); when(mapper.ownedForUpdate(7L, 11L)).thenReturn(deleted);
            assertThrows(BusinessException.class, () -> service.retire(11L, new RetireItemRequest(today, 0L, KEY)));
            verify(mapper, never()).updateById(any(PersonalItem.class));
        }
    }
    @Test void deletionWritesAuditAndRetryDoesNotWriteAgain() {
        try (var ignored = identity()) {
            var item = item(today); when(mapper.ownedForUpdate(7L, 11L)).thenReturn(item);
            when(mapper.softDelete(item)).thenReturn(1); service.delete(11L, KEY); service.delete(11L, KEY);
            assertEquals(1, item.getDeleted()); assertEquals(7L, item.getDeletedBy());
            assertEquals("测试用户", item.getDeletedName()); assertNotNull(item.getDeletedAt());
            verify(mapper, times(1)).softDelete(item);
        }
    }
    @Test void summaryContainsOnlyServingItemsAndPaginationPreservesTotals() {
        try (var ignored = identity()) {
            var a = item(today.minusDays(2)); var b = item(today.minusDays(1)); var retired = item(today);
            retired.setStatus("RETIRED"); retired.setRetiredOn(today); retired.setResaleCent(0L);
            when(mapper.listOwned(7L)).thenReturn(List.of(a, b, retired));
            var result = service.overview("ALL", 2, 1);
            assertEquals(20000, result.totalAssetsCents()); assertEquals(2, result.activeCount());
            assertEquals(new BigDecimal("8333.33333333"), result.totalDailyCostCents());
            assertEquals(3, result.total()); assertTrue(result.hasMore()); assertEquals(1, result.items().size());
            assertThrows(BusinessException.class, () -> service.overview("ALL", 0, 20));
        }
    }
}
