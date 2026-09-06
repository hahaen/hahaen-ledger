package com.hahaen.ledger.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** H5 注册和登录共用请求；密码字段始终是 RSA-OAEP 加密后的 Base64。 */
public record H5AuthRequest(
        @NotBlank @Size(max = 64) String account,
        @NotBlank @Size(max = 1024) String encryptedPassword,
        @NotBlank @Size(max = 64) String captchaId,
        @NotBlank @Size(max = 8) String captchaCode
) {
}
