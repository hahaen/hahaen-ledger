package com.hahaen.ledger.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.hahaen.ledger.common.security.CurrentUser;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** 统一处理主表和关联表的创建、更新审计字段。 */
@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        strictInsertFill(metaObject, "deleted", Integer.class, 0);
        if (hasField(metaObject, "createdBy")) {
            Long actorId = CurrentUser.optionalId();
            // 创建人可空：仅使用真实登录身份，不从业务记录 ID 推断。
            if (actorId != null) {
                strictInsertFill(metaObject, "createdBy", Long.class, actorId);
                strictInsertFill(metaObject, "createdName", String.class, CurrentUser.optionalName());
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        if (hasField(metaObject, "updatedBy")) {
            Long actorId = CurrentUser.optionalId();
            if (actorId != null) {
                strictUpdateFill(metaObject, "updatedBy", Long.class, actorId);
                strictUpdateFill(metaObject, "updateName", String.class, CurrentUser.optionalName());
            }
        }
    }

    private boolean hasField(MetaObject metaObject, String field) {
        return metaObject.hasSetter(field);
    }
}
