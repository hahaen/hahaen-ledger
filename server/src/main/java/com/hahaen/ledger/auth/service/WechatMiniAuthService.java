package com.hahaen.ledger.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hahaen.ledger.auth.dto.WechatMiniLoginRequest;
import com.hahaen.ledger.auth.vo.LoginVO;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.user.entity.AppLoginLog;
import com.hahaen.ledger.user.entity.AppUser;
import com.hahaen.ledger.user.entity.UserIdentity;
import com.hahaen.ledger.user.mapper.AppLoginLogMapper;
import com.hahaen.ledger.user.mapper.AppUserMapper;
import com.hahaen.ledger.user.mapper.UserIdentityMapper;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class WechatMiniAuthService {
    private static final String PROVIDER = "WECHAT_MINI_PROGRAM";
    private static final String ACTIVE = "ACTIVE";

    private final WechatCode2SessionClient code2SessionClient;
    private final UserIdentityMapper identityMapper;
    private final AppUserMapper userMapper;
    private final AppLoginLogMapper loginLogMapper;
    private final LoginAuditService loginAuditService;
    private final boolean loginEnabled;

    public WechatMiniAuthService(WechatCode2SessionClient code2SessionClient, UserIdentityMapper identityMapper,
                                 AppUserMapper userMapper, AppLoginLogMapper loginLogMapper,
                                 LoginAuditService loginAuditService,
                                 @Value("${hahaen.wechat.login-enabled:false}") boolean loginEnabled) {
        this.code2SessionClient = code2SessionClient;
        this.identityMapper = identityMapper;
        this.userMapper = userMapper;
        this.loginLogMapper = loginLogMapper;
        this.loginAuditService = loginAuditService;
        this.loginEnabled = loginEnabled;
    }

    @Transactional
    public LoginVO login(WechatMiniLoginRequest request, String ip, String userAgent) {
        if (!loginEnabled) {
            failure(ip, userAgent, "WECHAT_LOGIN_DISABLED");
            throw new BusinessException("WECHAT_LOGIN_DISABLED", "微信登录暂未开启");
        }

        String code;
        WechatCode2SessionClient.WechatCode2Session session;
        try {
            code = normalizeCode(request.code());
            session = code2SessionClient.exchange(code);
        } catch (BusinessException ex) {
            failure(ip, userAgent, ex.getErrorCode());
            throw ex;
        }
        if (session == null || session.errorCode() != null && session.errorCode() != 0
                || !hasText(session.openId()) || !hasText(session.sessionKey())) {
            failure(ip, userAgent, "WECHAT_CODE_INVALID");
            throw new BusinessException("WECHAT_CODE_INVALID", "微信登录凭证无效，请重试");
        }

        try {
            UserIdentity identity = identityMapper.selectOne(new LambdaQueryWrapper<UserIdentity>()
                    .eq(UserIdentity::getProvider, PROVIDER)
                    .eq(UserIdentity::getOpenId, session.openId())
                    .eq(UserIdentity::getDeleted, 0));
            AppUser user = identity == null ? createUser(session) : activeUser(identity.getUserId());
            if (user == null) {
                failure(ip, userAgent, "WECHAT_USER_UNAVAILABLE");
                throw new BusinessException("WECHAT_USER_UNAVAILABLE", "微信用户不可用，请重试");
            }
            user.setLastLoginAt(LocalDateTime.now());
            user.setLastLoginIp(trimIp(ip));
            userMapper.updateById(user);
            StpUtil.login(user.getId());
            StpUtil.getSession().set("auditName", user.getNickname());
            success(user, ip, userAgent);
            return new LoginVO(StpUtil.getTokenValue(), String.valueOf(user.getId()), user.getNickname());
        } catch (DuplicateKeyException ex) {
            failure(ip, userAgent, "WECHAT_IDENTITY_CONFLICT");
            throw new BusinessException("WECHAT_IDENTITY_CONFLICT", "微信登录发生并发冲突，请重试");
        }
    }

    private AppUser createUser(WechatCode2SessionClient.WechatCode2Session session) {
        AppUser user = new AppUser();
        user.setNickname("账本主人");
        user.setStatus(ACTIVE);
        userMapper.insert(user);

        UserIdentity identity = new UserIdentity();
        identity.setUserId(user.getId());
        identity.setProvider(PROVIDER);
        identity.setOpenId(session.openId());
        identity.setUnionId(session.unionId());
        identityMapper.insert(identity);
        return user;
    }

    private AppUser activeUser(Long userId) {
        if (userId == null) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getId, userId)
                .eq(AppUser::getStatus, ACTIVE)
                .eq(AppUser::getDeleted, 0));
    }

    private void success(AppUser user, String ip, String userAgent) {
        AppLoginLog log = baseLog(ip, userAgent);
        log.setUserId(user.getId());
        log.setLoginResult("SUCCESS");
        loginLogMapper.insert(log);
    }

    private void failure(String ip, String userAgent, String failureCode) {
        AppLoginLog log = baseLog(ip, userAgent);
        log.setLoginResult("FAILURE");
        log.setFailureCode(failureCode);
        loginAuditService.recordFailure(log);
    }

    private AppLoginLog baseLog(String ip, String userAgent) {
        AppLoginLog log = new AppLoginLog();
        log.setLoginChannel(PROVIDER);
        log.setLoginIp(trimIp(ip));
        log.setUserAgent(userAgent == null ? null : userAgent.substring(0, Math.min(512, userAgent.length())));
        log.setTraceId(MDC.get("traceId"));
        return log;
    }

    private static String normalizeCode(String code) {
        String normalized = code == null ? "" : code.trim();
        if (!hasText(normalized) || normalized.length() > 128) {
            throw new BusinessException("WECHAT_CODE_INVALID", "微信登录凭证无效，请重试");
        }
        return normalized;
    }

    private static String trimIp(String ip) {
        return ip == null ? null : ip.substring(0, Math.min(45, ip.length()));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
