package com.hahaen.ledger.asset.service;

import com.hahaen.ledger.account.entity.AssetAccount;
import com.hahaen.ledger.account.mapper.AssetAccountMapper;
import com.hahaen.ledger.common.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class AssetServiceTest {
    @Test
    void calculatesNetAssetsFromIncludedFundAndCreditAccounts() {
        AssetAccountMapper mapper = mock(AssetAccountMapper.class);
        when(mapper.selectActiveByUser(7L)).thenReturn(List.of(
                fund(1L, 10_000L, 1), fund(2L, 5_000L, 0), credit(3L, 8_000L, 2_500L, 1), credit(4L, 8_000L, 1_000L, 0)));
        AssetService service = new AssetService(mapper);
        try (MockedStatic<CurrentUser> ignored = mockStatic(CurrentUser.class)) {
            ignored.when(CurrentUser::id).thenReturn(7L);
            var result = service.overview();
            assertEquals(10_000L, result.totalAssetsCents());
            assertEquals(2_500L, result.totalLiabilitiesCents());
            assertEquals(7_500L, result.netAssetsCents());
            assertEquals(4, result.accounts().size());
        }
    }

    private static AssetAccount fund(long id, long balance, int included) {
        AssetAccount value = new AssetAccount(); value.setId(id); value.setAccountType("FUND"); value.setBalanceCent(balance); value.setIncludeNetAsset(included); return value;
    }

    private static AssetAccount credit(long id, long limit, long debt, int included) {
        AssetAccount value = new AssetAccount(); value.setId(id); value.setAccountType("CREDIT"); value.setTotalLimitCent(limit); value.setCurrentDebtCent(debt); value.setIncludeNetAsset(included); return value;
    }
}
