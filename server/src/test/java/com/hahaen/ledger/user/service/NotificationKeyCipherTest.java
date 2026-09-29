package com.hahaen.ledger.user.service;

import com.hahaen.ledger.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationKeyCipherTest {
    @Test
    void encryptsWithFreshNonceAndRestoresOnlyWithCorrectKey() {
        String key = Base64.getEncoder().encodeToString(new byte[32]);
        NotificationKeyCipher cipher = new NotificationKeyCipher(key);
        String first = cipher.encrypt("example-notification-key");
        String second = cipher.encrypt("example-notification-key");

        assertTrue(first.startsWith("v1:"));
        assertNotEquals(first, second);
        assertNotEquals("example-notification-key", first);
        assertEquals("example-notification-key", cipher.decrypt(first));
        byte[] differentKey = new byte[32];
        differentKey[0] = 1;
        assertThrows(IllegalStateException.class, () -> new NotificationKeyCipher(
                Base64.getEncoder().encodeToString(differentKey)).decrypt(first));
    }

    @Test
    void refusesToStoreKeyWithoutConfiguredAesKey() {
        BusinessException error = assertThrows(BusinessException.class,
                () -> new NotificationKeyCipher("").encrypt("example-notification-key"));
        assertEquals("NOTIFICATION_KEY_UNAVAILABLE", error.getErrorCode());
    }
}
