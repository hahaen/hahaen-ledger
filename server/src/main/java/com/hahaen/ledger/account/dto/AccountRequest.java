package com.hahaen.ledger.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccountRequest(
        @NotBlank(message = "账户名称不能为空")
        @Size(max = 20, message = "账户名称最多20个字符")
        String name,
        @NotBlank(message = "账户类型不能为空") String kind,
        Long balanceCents,
        Long creditLimitCents,
        Long currentDebtCents,
        Boolean includedInNetAsset) {
}
