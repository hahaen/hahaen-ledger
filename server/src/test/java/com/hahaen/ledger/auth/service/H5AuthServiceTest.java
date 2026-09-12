package com.hahaen.ledger.auth.service;

import com.hahaen.ledger.auth.dto.H5AuthRequest;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.user.entity.AppUser;
import com.hahaen.ledger.user.mapper.AppLoginLogMapper;
import com.hahaen.ledger.user.mapper.AppUserMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

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

        service(users, captcha, crypto, encoder).register(new H5AuthRequest(" Demo123 ", "encrypted", "captcha", "ABCD"), "127.0.0.1", "test-agent");

        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(users).insert(saved.capture());
        assertEquals("demo123", saved.getValue().getLoginAccount());
        assertEquals("demo123", saved.getValue().getNickname());
    }

    @Test
    void rejectsChineseAndPunctuationInAccountBeforeCallingAuthenticationServices() {
        AppUserMapper users = mock(AppUserMapper.class);
        CaptchaService captcha = mock(CaptchaService.class);
        H5AuthService service = service(users, captcha, mock(PasswordCryptoService.class), mock(PasswordEncoder.class));

        for (String account : new String[]{"中文账号", "demo.user", "demo-user", "demo_user"}) {
            BusinessException error = assertThrows(BusinessException.class,
                    () -> service.register(new H5AuthRequest(account, "encrypted", "captcha", "ABCD"), "127.0.0.1", "test-agent"));
            assertEquals("ACCOUNT_INVALID", error.getErrorCode());
        }

        verify(captcha, never()).consume(any(), any());
        verify(users, never()).insert(any(AppUser.class));
    }

    private static H5AuthService service(AppUserMapper users, CaptchaService captcha, PasswordCryptoService crypto, PasswordEncoder encoder) {
        return new H5AuthService(users, mock(AppLoginLogMapper.class), mock(LoginAuditService.class), encoder, crypto, captcha);
    }
}
