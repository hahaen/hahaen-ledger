package com.hahaen.ledger.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hahaen.ledger.auth.dto.H5AuthRequest;
import com.hahaen.ledger.auth.vo.LoginVO;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.user.entity.AppLoginLog;
import com.hahaen.ledger.user.entity.AppUser;
import com.hahaen.ledger.user.mapper.AppLoginLogMapper;
import com.hahaen.ledger.user.mapper.AppUserMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class H5AuthService {
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("[a-z0-9][a-z0-9._-]{1,63}");
    private static final int MAX_PASSWORD_BYTES = 72;

    private final AppUserMapper userMapper;
    private final AppLoginLogMapper loginLogMapper;
    private final LoginAuditService loginAuditService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordCryptoService passwordCryptoService;
    private final CaptchaService captchaService;

    @Transactional
    public void register(H5AuthRequest request, String ip, String userAgent) {
        String account = normalizeAccount(request.account());
        try {
            checkCaptcha(request, account, ip, userAgent);
            String password = validPassword(passwordCryptoService.decrypt(request.encryptedPassword()));
            if (userMapper.selectCount(new LambdaQueryWrapper<AppUser>().eq(AppUser::getLoginAccount, account)) > 0) {
                failure(account, ip, userAgent, "ACCOUNT_EXISTS");
                throw new BusinessException("ACCOUNT_EXISTS", "账号已存在，请更换后重试");
            }
            AppUser user = new AppUser();
            user.setLoginAccount(account);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setNickname("账本主人");
            user.setStatus("ACTIVE");
            userMapper.insert(user);
        } catch (DuplicateKeyException ex) {
            failure(account, ip, userAgent, "ACCOUNT_EXISTS");
            throw new BusinessException("ACCOUNT_EXISTS", "账号已存在，请更换后重试");
        }
    }

    @Transactional
    public LoginVO login(H5AuthRequest request, String ip, String userAgent) {
        String account = normalizeAccount(request.account());
        try {
            checkCaptcha(request, account, ip, userAgent);
            String password = validPassword(passwordCryptoService.decrypt(request.encryptedPassword()));
            AppUser user = userMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                    .eq(AppUser::getLoginAccount, account)
                    .eq(AppUser::getStatus, "ACTIVE"));
            if (user == null) {
                failure(account, ip, userAgent, "AUTH_FAILED");
                throw new BusinessException("AUTH_FAILED", "账号或密码错误");
            }
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                failure(account, ip, userAgent, "AUTH_FAILED");
                throw new BusinessException("AUTH_FAILED", "账号或密码错误");
            }
            user.setLastLoginAt(LocalDateTime.now());
            user.setLastLoginIp(ip);
            userMapper.updateById(user);
            StpUtil.login(user.getId());
            StpUtil.getSession().set("auditName", user.getNickname());
            success(user, account, ip, userAgent);
            return new LoginVO(StpUtil.getTokenValue(), user.getId(), user.getNickname());
        } catch (BusinessException ex) {
            if (!"AUTH_FAILED".equals(ex.getErrorCode()) && !"ACCOUNT_EXISTS".equals(ex.getErrorCode())) {
                failure(account, ip, userAgent, ex.getErrorCode());
            }
            throw ex;
        }
    }

    private void checkCaptcha(H5AuthRequest request, String account, String ip, String userAgent) {
        if (!captchaService.consume(request.captchaId(), request.captchaCode())) {
            failure(account, ip, userAgent, "CAPTCHA_INVALID");
            throw new BusinessException("CAPTCHA_INVALID", "图形验证码错误或已过期");
        }
    }

    private static String normalizeAccount(String input) {
        String account = input == null ? "" : input.trim().toLowerCase(Locale.ROOT);
        if (!ACCOUNT_PATTERN.matcher(account).matches()) {
            throw new BusinessException("ACCOUNT_INVALID", "账号需为 2-64 位字母、数字或 ._- 组合");
        }
        return account;
    }

    private static String validPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 64
                || password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new BusinessException("PASSWORD_INVALID", "密码需为 8-64 位字符");
        }
        return password;
    }

    private void success(AppUser user, String account, String ip, String userAgent) {
        AppLoginLog log = baseLog(account, ip, userAgent);
        log.setUserId(user.getId());
        log.setLoginResult("SUCCESS");
        loginLogMapper.insert(log);
    }

    private void failure(String account, String ip, String userAgent, String code) {
        AppLoginLog log = baseLog(account, ip, userAgent);
        log.setLoginResult("FAILURE");
        log.setFailureCode(code);
        loginAuditService.recordFailure(log);
    }

    private AppLoginLog baseLog(String account, String ip, String userAgent) {
        AppLoginLog log = new AppLoginLog();
        log.setLoginChannel("H5_PASSWORD");
        log.setLoginAccount(account);
        log.setLoginIp(ip);
        log.setUserAgent(userAgent == null ? null : userAgent.substring(0, Math.min(512, userAgent.length())));
        log.setTraceId(MDC.get("traceId"));
        return log;
    }
}
