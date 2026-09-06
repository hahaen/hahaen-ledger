package com.hahaen.ledger.common.security;

import cn.dev33.satoken.stp.StpUtil;
import com.hahaen.ledger.common.exception.BusinessException;

public final class CurrentUser {
    private CurrentUser() {}
    public static long id() {
        Long id = optionalId();
        if (id == null) throw new BusinessException("AUTH_REQUIRED", "请先登录");
        return id;
    }

    public static Long optionalId() {
        return StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
    }

    public static String optionalName() {
        return StpUtil.isLogin() ? StpUtil.getSession().getString("auditName") : null;
    }
}
