package com.hahaen.ledger.auth.service;

import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordCryptoServiceTest {
    @Test
    void decryptsRsaOaepSha256Password() throws Exception {
        PasswordCryptoService service = new PasswordCryptoService("");
        service.initialize();
        var publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(
                Base64.getDecoder().decode(service.publicKey())));
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
        String encrypted = Base64.getEncoder().encodeToString(cipher.doFinal("test-password".getBytes(StandardCharsets.UTF_8)));

        assertThat(service.decrypt(encrypted)).isEqualTo("test-password");
    }
}
