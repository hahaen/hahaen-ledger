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
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.MDC;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class H5AuthService {
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("[a-z0-9]{2,64}");
    private static final int MAX_PASSWORD_BYTES = 72;

    private final AppUserMapper userMapper;
    private final AppLoginLogMapper loginLogMapper;
    private final LoginAuditService loginAuditService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordCryptoService passwordCryptoService;
    private final CaptchaService captchaService;
    private final boolean allowInsecurePasswordOverHttp;

    public H5AuthService(AppUserMapper userMapper, AppLoginLogMapper loginLogMapper, LoginAuditService loginAuditService,
                         PasswordEncoder passwordEncoder, PasswordCryptoService passwordCryptoService, CaptchaService captchaService,
                         @Value("${hahaen.auth.allow-insecure-password-over-http:false}") boolean allowInsecurePasswordOverHttp) {
        this.userMapper = userMapper;
        this.loginLogMapper = loginLogMapper;
        this.loginAuditService = loginAuditService;
        this.passwordEncoder = passwordEncoder;
        this.passwordCryptoService = passwordCryptoService;
        this.captchaService = captchaService;
        this.allowInsecurePasswordOverHttp = allowInsecurePasswordOverHttp;
    }

    @Transactional
    public void register(H5AuthRequest request, String ip, String userAgent, boolean secureTransport) {
        String account = normalizeAccount(request.account());
        try {
            checkCaptcha(request, account, ip, userAgent);
            String password = validPassword(resolvePassword(request, secureTransport));
            if (userMapper.selectCount(new LambdaQueryWrapper<AppUser>().eq(AppUser::getLoginAccount, account)) > 0) {
                failure(account, ip, userAgent, "ACCOUNT_EXISTS");
                throw new BusinessException("ACCOUNT_EXISTS", "账号已存在，请更换后重试");
            }
            AppUser user = new AppUser();
            user.setLoginAccount(account);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setNickname(account);
            user.setStatus("ACTIVE");
            userMapper.insert(user);
        } catch (DuplicateKeyException ex) {
            failure(account, ip, userAgent, "ACCOUNT_EXISTS");
            throw new BusinessException("ACCOUNT_EXISTS", "账号已存在，请更换后重试");
        }
    }

    @Transactional
    public LoginVO login(H5AuthRequest request, String ip, String userAgent, boolean secureTransport) {
        String account = normalizeAccount(request.account());
        try {
            checkCaptcha(request, account, ip, userAgent);
            String password = validPassword(resolvePassword(request, secureTransport));
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
            return new LoginVO(StpUtil.getTokenValue(), String.valueOf(user.getId()), user.getNickname());
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

    public boolean allowsInsecurePasswordOverHttp() {
        return allowInsecurePasswordOverHttp;
    }

    private String resolvePassword(H5AuthRequest request, boolean secureTransport) {
        boolean hasEncryptedPassword = hasText(request.encryptedPassword());
        boolean hasCompatibilityPassword = hasText(request.compatibilityPassword());
        if (hasEncryptedPassword == hasCompatibilityPassword) {
            throw new BusinessException("PASSWORD_PAYLOAD_INVALID", "密码参数不正确，请重试");
        }
        if (hasEncryptedPassword) return passwordCryptoService.decrypt(request.encryptedPassword());
        if (!allowInsecurePasswordOverHttp || secureTransport) {
            throw new BusinessException("INSECURE_PASSWORD_DISABLED", "当前服务器未开启 HTTP 临时密码兼容");
        }
        return decodeHttpCompatibilityPassword(request.compatibilityPassword());
    }

    private static String decodeHttpCompatibilityPassword(String compatibilityPassword) {
        try {
            byte[] encoded = Base64.getDecoder().decode(compatibilityPassword);
            byte[] key = "haji-http-temp-v1".getBytes(StandardCharsets.UTF_8);
            if (encoded.length == 0 || encoded.length > 128) throw new IllegalArgumentException("兼容载荷长度不正确");
            for (int index = 0; index < encoded.length; index += 1) encoded[index] ^= key[index % key.length];
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new BusinessException("PASSWORD_INVALID", "密码格式不正确，请重试");
        }
    }

    private static String normalizeAccount(String input) {
        String account = input == null ? "" : input.trim().toLowerCase(Locale.ROOT);
        if (!ACCOUNT_PATTERN.matcher(account).matches()) {
            throw new BusinessException("ACCOUNT_INVALID", "账号需为 2-64 位英文字母或数字");
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

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
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
