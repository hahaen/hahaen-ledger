package com.hahaen.ledger.common.security;

/** 系统自动创建数据时使用的集中身份定义，禁止在业务模块散落魔法用户ID。 */
public final class AuditIdentity {
    public static final long SYSTEM_USER_ID = 0L;

    private AuditIdentity() {
    }
}
