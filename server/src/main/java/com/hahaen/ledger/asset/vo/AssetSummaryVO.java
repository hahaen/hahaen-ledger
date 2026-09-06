package com.hahaen.ledger.asset.vo;
import com.hahaen.ledger.account.entity.LedgerAccount;
import java.util.List;
public record AssetSummaryVO(long netAssetCents, long totalAssetCents, long totalLiabilityCents, List<LedgerAccount> fundAccounts, List<LedgerAccount> creditAccounts) {}
