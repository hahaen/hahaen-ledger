package com.hahaen.ledger.asset.service;
import com.hahaen.ledger.account.entity.LedgerAccount;
import com.hahaen.ledger.account.service.AccountService;
import com.hahaen.ledger.asset.vo.AssetSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service @RequiredArgsConstructor
public class AssetService {
    private final AccountService accountService;
    public AssetSummaryVO summary() { List<LedgerAccount> all=accountService.list(); List<LedgerAccount> funds=all.stream().filter(a->a.getKind().equals("FUND")).toList(); List<LedgerAccount> credits=all.stream().filter(a->a.getKind().equals("CREDIT")).toList(); long assets=funds.stream().filter(LedgerAccount::getIncludedInNetAsset).mapToLong(LedgerAccount::getBalanceCents).sum(); long debt=credits.stream().filter(LedgerAccount::getIncludedInNetAsset).mapToLong(LedgerAccount::getBalanceCents).sum(); return new AssetSummaryVO(assets-debt,assets,debt,funds,credits); }
}
