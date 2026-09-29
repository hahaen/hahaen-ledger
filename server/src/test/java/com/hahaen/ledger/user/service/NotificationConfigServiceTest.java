package com.hahaen.ledger.user.service;

import com.hahaen.ledger.auth.service.PasswordCryptoService;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.CurrentUser;
import com.hahaen.ledger.user.dto.NotificationConfigRequest;
import com.hahaen.ledger.user.entity.UserNotificationConfig;
import com.hahaen.ledger.user.mapper.UserNotificationConfigMapper;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationConfigServiceTest {
    @Test
    void storesOnlyEncryptedKeyForCurrentUser() throws Exception {
        UserNotificationConfigMapper mapper = mock(UserNotificationConfigMapper.class);
        PasswordCryptoService rsa = new PasswordCryptoService("");
        ReflectionTestUtils.invokeMethod(rsa, "initialize");
        NotificationKeyCipher aes = new NotificationKeyCipher(Base64.getEncoder().encodeToString(new byte[32]));
        when(mapper.lockActiveUser(7L)).thenReturn(7L);
        when(mapper.insert(any(UserNotificationConfig.class))).thenAnswer(invocation -> {
            UserNotificationConfig row = invocation.getArgument(0);
            assertEquals(7L, row.getUserId());
            assertEquals("BARK", row.getNotificationType());
            assertEquals("example-notification-key", aes.decrypt(row.getNotificationKey()));
            assertFalse(row.getNotificationKey().contains("example-notification-key"));
            return 1;
        });
        when(mapper.insertRequest(eq(7L), eq("request-123"), any())).thenReturn(1);

        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            boolean configured = new NotificationConfigService(mapper, rsa, aes)
                    .save("BARK", new NotificationConfigRequest(encrypt(rsa, "example-notification-key"), false, "request-123"))
                    .configured();
            assertTrue(configured);
        }
    }

    @Test
    void removeRequiresCurrentActiveUserAndWritesAuditFields() {
        UserNotificationConfigMapper mapper = mock(UserNotificationConfigMapper.class);
        UserNotificationConfig row = new UserNotificationConfig();
        row.setId(11L);
        row.setDeleted(0);
        when(mapper.lockActiveUser(7L)).thenReturn(7L);
        when(mapper.findIncludingDeleted(7L, "PUSHPLUS")).thenReturn(row);
        when(mapper.softDelete(11L, 7L, "测试用户")).thenReturn(1);
        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            current.when(CurrentUser::optionalName).thenReturn("测试用户");
            boolean configured = new NotificationConfigService(mapper, mock(PasswordCryptoService.class), mock(NotificationKeyCipher.class))
                    .save("PUSHPLUS", new NotificationConfigRequest(null, true, "request-456"))
                    .configured();
            assertFalse(configured);
            verify(mapper).softDelete(11L, 7L, "测试用户");
        }
    }

    @Test
    void rejectsInactiveUserBeforeReadingOrWritingConfig() {
        UserNotificationConfigMapper mapper = mock(UserNotificationConfigMapper.class);
        when(mapper.lockActiveUser(7L)).thenReturn(null);
        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            BusinessException error = assertThrows(BusinessException.class, () ->
                    new NotificationConfigService(mapper, mock(PasswordCryptoService.class), mock(NotificationKeyCipher.class))
                            .save("BARK", new NotificationConfigRequest(null, true, "request-789")));
            assertEquals("USER_NOT_FOUND", error.getErrorCode());
        }
        verify(mapper, never()).findIncludingDeleted(any(Long.class), any(String.class));
    }

    private static String encrypt(PasswordCryptoService rsa, String plain) throws Exception {
        var publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(
                Base64.getDecoder().decode(rsa.publicKey())));
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
        return Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
    }
}
