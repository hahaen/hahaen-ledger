package com.hahaen.ledger.user.vo;

import java.time.LocalDateTime;

public record ProfileVO(
        String userId,
        String nickname,
        LocalDateTime createdAt,
        long cumulativeDays,
        boolean avatarAuthorized,
        String avatarFileId
) {
}
