package com.hahaen.ledger.user.service;

import com.hahaen.ledger.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/** 可供将来的通知发送服务解密；密钥只从运行环境读取，不进入数据库或响应。 */
@Service
public class NotificationKeyCipher {
    private static final int NONCE_LENGTH = 12;
    private static final String VERSION = "v1:";
    private final String configuredKey;
    private final SecureRandom random = new SecureRandom();

    public NotificationKeyCipher(@Value("${hahaen.notification.key-aes-base64:}") String configuredKey) {
        this.configuredKey = configuredKey;
    }

    public String encrypt(String value) {
        try {
            byte[] nonce = new byte[NONCE_LENGTH];
            random.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(128, nonce));
            byte[] ciphertext = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] output = Arrays.copyOf(nonce, nonce.length + ciphertext.length);
            System.arraycopy(ciphertext, 0, output, nonce.length, ciphertext.length);
            return VERSION + Base64.getEncoder().encodeToString(output);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("通知Key加密失败", ex);
        }
    }

    public String decrypt(String value) {
        try {
            if (value == null || !value.startsWith(VERSION)) throw new IllegalArgumentException("未知密文版本");
            byte[] input = Base64.getDecoder().decode(value.substring(VERSION.length()));
            if (input.length < NONCE_LENGTH + 16) throw new IllegalArgumentException("密文过短");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, input, 0, NONCE_LENGTH));
            return new String(cipher.doFinal(input, NONCE_LENGTH, input.length - NONCE_LENGTH), StandardCharsets.UTF_8);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("通知Key解密失败", ex);
        }
    }

    private SecretKeySpec key() {
        try {
            byte[] bytes = Base64.getDecoder().decode(configuredKey);
            if (bytes.length != 32) throw new IllegalArgumentException("AES-256 requires 32 bytes");
            return new SecretKeySpec(bytes, "AES");
        } catch (Exception ex) {
            throw new BusinessException("NOTIFICATION_KEY_UNAVAILABLE", "通知Key加密配置不可用");
        }
    }
}
