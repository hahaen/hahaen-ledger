package com.hahaen.ledger.account.service;

import com.hahaen.ledger.account.dto.AccountRequest;
import com.hahaen.ledger.account.entity.AssetAccount;
import com.hahaen.ledger.account.mapper.AssetAccountMapper;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTest {
    @Test
    void createsFundAccountWithFundOnlyAmountColumns() {
        AssetAccountMapper mapper = mock(AssetAccountMapper.class);
        doAnswer(invocation -> { AssetAccount value = invocation.getArgument(0); value.setId(11L); return 1; }).when(mapper).insert(any(AssetAccount.class));
        AccountService service = new AccountService(mapper);
        try (MockedStatic<CurrentUser> ignored = mockStatic(CurrentUser.class)) {
            ignored.when(CurrentUser::id).thenReturn(7L);
            var result = service.create(new AccountRequest("微信", "FUND", 12_345L, 99L, 99L, true));
            assertEquals("11", result.id());
            assertEquals("FUND", result.kind());
            assertEquals(12_345L, result.balanceCents());
            assertEquals(0L, result.creditLimitCents());
            assertEquals(1, result.sortOrder());
            ArgumentCaptor<AssetAccount> captor = ArgumentCaptor.forClass(AssetAccount.class);
            verify(mapper).insert(captor.capture());
            assertEquals(null, captor.getValue().getTotalLimitCent());
            assertEquals(null, captor.getValue().getCurrentDebtCent());
        }
    }

    @Test
    void rejectsCreditDebtAboveLimitBeforePersistence() {
        AssetAccountMapper mapper = mock(AssetAccountMapper.class);
        AccountService service = new AccountService(mapper);
        try (MockedStatic<CurrentUser> ignored = mockStatic(CurrentUser.class)) {
            ignored.when(CurrentUser::id).thenReturn(7L);
            assertThrows(BusinessException.class, () -> service.create(new AccountRequest("信用卡", "CREDIT", null, 10_000L, 10_001L, true)));
            verify(mapper, never()).insert(any(AssetAccount.class));
        }
    }

    @Test
    void rejectsCrossUserAccountEvenWhenIdExists() {
        AssetAccountMapper mapper = mock(AssetAccountMapper.class);
        when(mapper.selectOwnedForUpdate(99L, 7L)).thenReturn(null);
        AccountService service = new AccountService(mapper);
        try (MockedStatic<CurrentUser> ignored = mockStatic(CurrentUser.class)) {
            ignored.when(CurrentUser::id).thenReturn(7L);
            assertThrows(BusinessException.class, () -> service.update(99L, new AccountRequest("账户", "FUND", 1L, null, null, true)));
        }
    }

    @Test
    void swapsOnlyAccountsOfSameKindAndRepeatedRequestIsIdempotent() {
        AssetAccountMapper mapper = mock(AssetAccountMapper.class);
        AssetAccount first = account(10L, "微信", "FUND", 1);
        AssetAccount second = account(20L, "银行卡", "FUND", 2);
        when(mapper.selectOwnedForUpdate(10L, 7L)).thenReturn(first);
        when(mapper.selectOwnedForUpdate(20L, 7L)).thenReturn(second);
        AccountService service = new AccountService(mapper);
        var request = new com.hahaen.ledger.account.dto.AccountOrderRequest(10L, 2, 1, "order-20-10-2-1");
        try (MockedStatic<CurrentUser> ignored = mockStatic(CurrentUser.class)) {
            ignored.when(CurrentUser::id).thenReturn(7L);
            service.reorder(20L, request);
            assertEquals(1, second.getSortOrder());
            assertEquals(2, first.getSortOrder());
            service.reorder(20L, request);
            verify(mapper, times(2)).updateById(any(AssetAccount.class));
        }
    }

    private static AssetAccount account(long id, String name, String kind, int sortOrder) {
        AssetAccount account = new AssetAccount();
        account.setId(id);
        account.setAccountName(name);
        account.setAccountType(kind);
        account.setSortOrder(sortOrder);
        account.setIncludeNetAsset(1);
        account.setBalanceCent(1L);
        return account;
    }
}
