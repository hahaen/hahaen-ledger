package com.hahaen.ledger.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank(message = "昵称不能为空") @Size(max = 40, message = "昵称最多40个字符") String nickname,
        @NotBlank(message = "账号不能为空") @Size(max = 64, message = "账号最多64个字符") String loginAccount,
        @Size(max = 1024, message = "密码参数过长") String encryptedPassword,
        @Pattern(regexp = "\\d+", message = "头像文件参数无效") String avatarFileId
) {
}
