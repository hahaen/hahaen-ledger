package com.hahaen.ledger.auth.dto;

import jakarta.validation.constraints.Size;

/** H5 注册和登录共用请求；密码默认使用 RSA-OAEP，兼容字段只承载 HTTP 临时混淆载荷。 */
public record H5AuthRequest(
        @jakarta.validation.constraints.NotBlank @Size(max = 64) String account,
        @Size(max = 1024) String encryptedPassword,
        @Size(max = 1024) String compatibilityPassword,
        @jakarta.validation.constraints.NotBlank @Size(max = 64) String captchaId,
        @jakarta.validation.constraints.NotBlank @Size(max = 8) String captchaCode
) {
}
