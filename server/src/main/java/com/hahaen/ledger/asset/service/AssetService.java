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
        long assets = 0;
        long liabilities = 0;
        for (AssetAccount account : accounts) {
            if (!Integer.valueOf(1).equals(account.getIncludeNetAsset())) continue;
            long balance = "FUND".equals(account.getAccountType())
                    ? value(account.getBalanceCent()) : value(account.getCurrentDebtCent());
            if (balance >= 0) {
                if ("FUND".equals(account.getAccountType())) assets = Math.addExact(assets, balance);
                else liabilities = Math.addExact(liabilities, balance);
            } else {
                long magnitude = Math.negateExact(balance);
                if ("FUND".equals(account.getAccountType())) liabilities = Math.addExact(liabilities, magnitude);
                else assets = Math.addExact(assets, magnitude);
            }
        }
        return new AssetOverviewVO(assets, liabilities, assets - liabilities,
                accounts.stream().map(AccountService::toVO).toList());
    }

    private static long value(Long value) {
        return value == null ? 0 : value;
    }
}
