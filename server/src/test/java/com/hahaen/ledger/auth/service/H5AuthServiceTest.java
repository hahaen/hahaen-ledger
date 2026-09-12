package com.hahaen.ledger.auth.service;

import com.hahaen.ledger.auth.dto.H5AuthRequest;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.user.entity.AppUser;
import com.hahaen.ledger.user.mapper.AppLoginLogMapper;
import com.hahaen.ledger.user.mapper.AppUserMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class H5AuthServiceTest {
    @Test
    void registrationUsesNormalizedAccountAsTheInitialNickname() {
        AppUserMapper users = mock(AppUserMapper.class);
        CaptchaService captcha = mock(CaptchaService.class);
        PasswordCryptoService crypto = mock(PasswordCryptoService.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(captcha.consume("captcha", "ABCD")).thenReturn(true);
        when(crypto.decrypt("encrypted")).thenReturn("password-8");
        when(encoder.encode("password-8")).thenReturn("password-hash");
        when(users.selectCount(any())).thenReturn(0L);

        service(users, captcha, crypto, encoder, false).register(new H5AuthRequest(" Demo123 ", "encrypted", null, "captcha", "ABCD"), "127.0.0.1", "test-agent", true);

        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(users).insert(saved.capture());
        assertEquals("demo123", saved.getValue().getLoginAccount());
        assertEquals("demo123", saved.getValue().getNickname());
    }

    @Test
    void rejectsChineseAndPunctuationInAccountBeforeCallingAuthenticationServices() {
        AppUserMapper users = mock(AppUserMapper.class);
        CaptchaService captcha = mock(CaptchaService.class);
        H5AuthService service = service(users, captcha, mock(PasswordCryptoService.class), mock(PasswordEncoder.class), false);

        for (String account : new String[]{"中文账号", "demo.user", "demo-user", "demo_user"}) {
            BusinessException error = assertThrows(BusinessException.class,
                    () -> service.register(new H5AuthRequest(account, "encrypted", null, "captcha", "ABCD"), "127.0.0.1", "test-agent", true));
            assertEquals("ACCOUNT_INVALID", error.getErrorCode());
        }

        verify(captcha, never()).consume(any(), any());
        verify(users, never()).insert(any(AppUser.class));
    }

    @Test
    void acceptsCompatibilityPasswordOnlyForExplicitHttpCompatibilityMode() {
        AppUserMapper users = mock(AppUserMapper.class);
        CaptchaService captcha = mock(CaptchaService.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(captcha.consume("captcha", "ABCD")).thenReturn(true);
        when(users.selectCount(any())).thenReturn(0L);
        when(encoder.encode("password-8")).thenReturn("password-hash");

        service(users, captcha, mock(PasswordCryptoService.class), encoder, true)
                .register(new H5AuthRequest("demo123", null, compatibilityPassword("password-8"), "captcha", "ABCD"), "127.0.0.1", "test-agent", false);

        verify(users).insert(any(AppUser.class));
    }

    @Test
    void rejectsCompatibilityPasswordWhenCompatibilityIsDisabledOrTransportIsHttps() {
        for (boolean compatibilityEnabled : new boolean[]{false, true}) {
            AppUserMapper users = mock(AppUserMapper.class);
            CaptchaService captcha = mock(CaptchaService.class);
            when(captcha.consume("captcha", "ABCD")).thenReturn(true);
            H5AuthService service = service(users, captcha, mock(PasswordCryptoService.class), mock(PasswordEncoder.class), compatibilityEnabled);

            boolean secureTransport = compatibilityEnabled;
            BusinessException error = assertThrows(BusinessException.class,
                    () -> service.register(new H5AuthRequest("demo123", null, compatibilityPassword("password-8"), "captcha", "ABCD"), "127.0.0.1", "test-agent", secureTransport));
            assertEquals("INSECURE_PASSWORD_DISABLED", error.getErrorCode());
            verify(users, never()).insert(any(AppUser.class));
        }
    }

    private static H5AuthService service(AppUserMapper users, CaptchaService captcha, PasswordCryptoService crypto, PasswordEncoder encoder, boolean allowInsecurePasswordOverHttp) {
        return new H5AuthService(users, mock(AppLoginLogMapper.class), mock(LoginAuditService.class), encoder, crypto, captcha, allowInsecurePasswordOverHttp);
    }

    private static String compatibilityPassword(String password) {
        byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] key = "haji-http-temp-v1".getBytes(StandardCharsets.UTF_8);
        for (int index = 0; index < bytes.length; index += 1) bytes[index] ^= key[index % key.length];
        return Base64.getEncoder().encodeToString(bytes);
    }
}
