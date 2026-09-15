package com.hahaen.ledger.auth.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.hahaen.ledger.auth.dto.WechatMiniLoginRequest;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.user.entity.AppLoginLog;
import com.hahaen.ledger.user.entity.AppUser;
import com.hahaen.ledger.user.entity.UserIdentity;
import com.hahaen.ledger.user.mapper.AppLoginLogMapper;
import com.hahaen.ledger.user.mapper.AppUserMapper;
import com.hahaen.ledger.user.mapper.UserIdentityMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

class WechatMiniAuthServiceTest {
    @Test
    void firstLoginCreatesUserAndIdentityWithoutWechatProfileData() {
        Fixtures fixtures = new Fixtures();
        when(fixtures.identities.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> {
            invocation.<AppUser>getArgument(0).setId(100L);
            return 1;
        }).when(fixtures.users).insert(any(AppUser.class));

        try (var stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getSession).thenReturn(new SaSession("session-id"));
            stp.when(StpUtil::getTokenValue).thenReturn("business-token");

            var result = fixtures.service.login(new WechatMiniLoginRequest("code-1"), "127.0.0.1", "wechat-agent");

            assertEquals("business-token", result.token());
            assertEquals("100", result.userId());
            assertEquals("账本主人", result.nickname());
            var user = org.mockito.ArgumentCaptor.forClass(AppUser.class);
            verify(fixtures.users).insert(user.capture());
            assertEquals(null, user.getValue().getLoginAccount());
            assertEquals(null, user.getValue().getPasswordHash());
            assertEquals("账本主人", user.getValue().getNickname());
            var identity = org.mockito.ArgumentCaptor.forClass(UserIdentity.class);
            verify(fixtures.identities).insert(identity.capture());
            assertEquals("WECHAT_MINI_PROGRAM", identity.getValue().getProvider());
            assertEquals("openid-1", identity.getValue().getOpenId());
            verify(fixtures.loginLogs).insert(any(AppLoginLog.class));
        }
    }

    @Test
    void existingIdentityLogsIntoTheExistingActiveUser() {
        Fixtures fixtures = new Fixtures();
        UserIdentity identity = new UserIdentity();
        identity.setUserId(200L);
        when(fixtures.identities.selectOne(any())).thenReturn(identity);
        AppUser user = new AppUser();
        user.setId(200L);
        user.setNickname("已有用户");
        user.setStatus("ACTIVE");
        when(fixtures.users.selectOne(any())).thenReturn(user);

        try (var stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getSession).thenReturn(new SaSession("session-id"));
            stp.when(StpUtil::getTokenValue).thenReturn("existing-token");

            var result = fixtures.service.login(new WechatMiniLoginRequest("code-2"), null, null);

            assertEquals("existing-token", result.token());
            assertEquals("已有用户", result.nickname());
            verify(fixtures.users, never()).insert(any(AppUser.class));
            verify(fixtures.identities, never()).insert(any(UserIdentity.class));
            verify(fixtures.users).updateById(user);
        }
    }

    @Test
    void invalidWechatCodeIsRejectedAndAuditedWithoutPersistingIdentity() {
        Fixtures fixtures = new Fixtures();
        when(fixtures.client.exchange("bad-code"))
                .thenReturn(new WechatCode2SessionClient.WechatCode2Session(null, null, null, 40029, "invalid code"));

        BusinessException error = assertThrows(BusinessException.class,
                () -> fixtures.service.login(new WechatMiniLoginRequest("bad-code"), "127.0.0.1", "agent"));

        assertEquals("WECHAT_CODE_INVALID", error.getErrorCode());
        verify(fixtures.audit).recordFailure(any(AppLoginLog.class));
        verify(fixtures.users, never()).insert(any(AppUser.class));
        verify(fixtures.identities, never()).insert(any(UserIdentity.class));
    }

    @Test
    void disabledWechatLoginIsRejectedBeforeCallingWechat() {
        Fixtures fixtures = new Fixtures(false);

        BusinessException error = assertThrows(BusinessException.class,
                () -> fixtures.service.login(new WechatMiniLoginRequest("code"), "127.0.0.1", "agent"));

        assertEquals("WECHAT_LOGIN_DISABLED", error.getErrorCode());
        verify(fixtures.client, never()).exchange(any());
        verify(fixtures.audit).recordFailure(any(AppLoginLog.class));
    }

    private static final class Fixtures {
        private final WechatCode2SessionClient client = mock(WechatCode2SessionClient.class);
        private final UserIdentityMapper identities = mock(UserIdentityMapper.class);
        private final AppUserMapper users = mock(AppUserMapper.class);
        private final AppLoginLogMapper loginLogs = mock(AppLoginLogMapper.class);
        private final LoginAuditService audit = mock(LoginAuditService.class);
        private final WechatMiniAuthService service;

        private Fixtures() {
            this(true);
        }

        private Fixtures(boolean loginEnabled) {
            when(client.exchange(any())).thenReturn(new WechatCode2SessionClient.WechatCode2Session(
                    "openid-1", "session-key-1", "unionid-1", null, null));
            service = new WechatMiniAuthService(client, identities, users, loginLogs, audit, loginEnabled);
        }
    }
}
