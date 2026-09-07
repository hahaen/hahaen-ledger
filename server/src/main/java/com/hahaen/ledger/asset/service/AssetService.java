package com.hahaen.ledger.asset.service;

import com.hahaen.ledger.account.entity.AssetAccount;
import com.hahaen.ledger.account.mapper.AssetAccountMapper;
import com.hahaen.ledger.account.service.AccountService;
import com.hahaen.ledger.account.vo.AssetOverviewVO;
import com.hahaen.ledger.common.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetService {
    private final AssetAccountMapper accountMapper;

    public AssetOverviewVO overview() {
        List<AssetAccount> accounts = accountMapper.selectActiveByUser(CurrentUser.id());
        long assets = accounts.stream()
                .filter(a -> "FUND".equals(a.getAccountType()) && Integer.valueOf(1).equals(a.getIncludeNetAsset()))
                .mapToLong(a -> value(a.getBalanceCent())).sum();
        long liabilities = accounts.stream()
                .filter(a -> "CREDIT".equals(a.getAccountType()) && Integer.valueOf(1).equals(a.getIncludeNetAsset()))
                .mapToLong(a -> value(a.getCurrentDebtCent())).sum();
        return new AssetOverviewVO(assets, liabilities, assets - liabilities,
                accounts.stream().map(AccountService::toVO).toList());
    }

    private static long value(Long value) {
        return value == null ? 0 : value;
    }
}
