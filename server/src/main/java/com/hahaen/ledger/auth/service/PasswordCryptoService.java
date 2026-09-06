package com.hahaen.ledger.auth.service;

import com.hahaen.ledger.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Service
public class PasswordCryptoService {
    private static final OAEPParameterSpec OAEP_SHA256 = new OAEPParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

    private final String configuredPrivateKey;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public PasswordCryptoService(@Value("${hahaen.auth.password-rsa-private-key:}") String configuredPrivateKey) {
        this.configuredPrivateKey = configuredPrivateKey;
    }

    @PostConstruct
    void initialize() {
        try {
            if (configuredPrivateKey == null || configuredPrivateKey.isBlank()
                    || "GENERATE".equalsIgnoreCase(configuredPrivateKey.trim())) {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair pair = generator.generateKeyPair();
                privateKey = pair.getPrivate();
                publicKey = pair.getPublic();
                return;
            }
            byte[] encoded = decodeConfiguredKey(configuredPrivateKey);
            privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encoded));
            var rsaPrivateKey = (java.security.interfaces.RSAPrivateCrtKey) privateKey;
            publicKey = java.security.KeyFactory.getInstance("RSA").generatePublic(
                    new java.security.spec.RSAPublicKeySpec(rsaPrivateKey.getModulus(), rsaPrivateKey.getPublicExponent()));
        } catch (Exception ex) {
            throw new IllegalStateException("H5 密码加密密钥配置无效", ex);
        }
    }

    public String publicKey() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public String decrypt(String encryptedPassword) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(encryptedPassword);
            if (encrypted.length > 512) throw new IllegalArgumentException("密文过长");
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey, OAEP_SHA256);
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new BusinessException("PASSWORD_INVALID", "密码格式不正确，请重试");
        }
    }

    private static byte[] decodeConfiguredKey(String configured) {
        String value = configured.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(value);
    }
}
