package com.hahaen.ledger.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 密码字段为 RSA-OAEP 加密后的 Base64；首次设置账号时一并携带 loginAccount。 */
public record ProfilePasswordUpdateRequest(
        @NotBlank(message = "密码不能为空") @Size(max = 1024, message = "密码参数过长") String encryptedPassword,
        @Size(max = 64, message = "账号最多64个字符") String loginAccount
) {
}
