package com.hahaen.ledger.common.security;

import com.hahaen.ledger.common.entity.BaseAuditEntity;

import java.time.LocalDateTime;

/** 主表逻辑删除的统一审计入口。 */
public final class AuditSupport {
    private AuditSupport() {
    }

    public static void markDeleted(BaseAuditEntity entity) {
        entity.setDeleted(1);
        entity.setDeletedAt(LocalDateTime.now());
        entity.setDeletedBy(CurrentUser.optionalId());
        entity.setDeletedName(CurrentUser.optionalName());
    }
}
