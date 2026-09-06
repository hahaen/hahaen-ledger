package com.hahaen.ledger.user.vo;

import java.time.LocalDateTime;

public record ProfileVO(
        Long userId,
        String nickname,
        LocalDateTime createdAt,
        long cumulativeDays,
        boolean avatarAuthorized,
        Long avatarFileId
) {
}
