package com.hahaen.ledger.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WechatMiniLoginRequest(
        @NotBlank @Size(max = 128) String code
) {
}
