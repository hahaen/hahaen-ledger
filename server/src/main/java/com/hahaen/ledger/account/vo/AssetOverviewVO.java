package com.hahaen.ledger.account.vo;

import java.util.List;

public record AssetOverviewVO(
        long totalAssetsCents,
        long totalLiabilitiesCents,
        long netAssetsCents,
        List<AccountVO> accounts) {
}
