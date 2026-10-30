package com.hahaen.ledger.common.security;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.exception.SaTokenContextException;
import com.hahaen.ledger.common.exception.BusinessException;

public final class CurrentUser {
    private CurrentUser() {}
    public static long id() {
        Long id = optionalId();
        if (id == null) throw new BusinessException("AUTH_REQUIRED", "请先登录");
        return id;
    }

    public static Long optionalId() {
        try { return StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null; }
        catch (SaTokenContextException ex) { return null; } // 调度线程没有HTTP上下文，不代表存在用户身份。
    }

    public static String optionalName() {
        try { return StpUtil.isLogin() ? StpUtil.getSession().getString("auditName") : null; }
        catch (SaTokenContextException ex) { return null; }
    }
}
