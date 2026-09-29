package com.hahaen.ledger.user;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hahaen.ledger.user.service.NotificationKeyCipher;
import com.hahaen.ledger.user.entity.UserNotificationConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 显式运行的隔离 DEV 验收；仅使用合成账号和通知 Key，结束时清理。 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"logging.file.path=target/notification-dev-logs"})
@ActiveProfiles("dev")
@EnabledIfSystemProperty(named = "notification.dev.verify", matches = "true")
class NotificationConfigDevIntegrationTest {
    @Autowired TestRestTemplate http;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;
    @Autowired NotificationKeyCipher keyCipher;

    @Test
    void migrationAuthEncryptionOwnershipDeleteAndRestore() throws Exception {
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM haji_flyway_history WHERE version='9' AND success=1", Integer.class));
        var columns = jdbc.queryForList("SELECT COLUMN_NAME, COLUMN_COMMENT, IS_NULLABLE, COLUMN_DEFAULT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user_notification_config'");
        assertEquals(Set.of("id", "user_id", "notification_type", "notification_key", "created_at", "created_by", "created_name", "updated_at", "updated_by", "update_name", "deleted_at", "deleted_by", "deleted_name", "deleted"),
                columns.stream().map(row -> row.get("COLUMN_NAME").toString()).collect(Collectors.toSet()));
        Set<String> entityColumns = new HashSet<>();
        for (Class<?> type = UserNotificationConfig.class; type != Object.class; type = type.getSuperclass()) {
            for (var field : type.getDeclaredFields()) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    entityColumns.add(field.getName().replaceAll("([A-Z])", "_$1").toLowerCase(Locale.ROOT));
                }
            }
        }
        assertEquals(entityColumns, columns.stream().map(row -> row.get("COLUMN_NAME").toString()).collect(Collectors.toSet()));
        for (var row : columns) assertFalse(row.get("COLUMN_COMMENT").toString().isBlank());
        assertEquals("utf8mb4_general_ci", jdbc.queryForObject("SELECT TABLE_COLLATION FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user_notification_config'", String.class));
        assertEquals("0", columns.stream().filter(row -> "deleted".equals(row.get("COLUMN_NAME"))).findFirst().orElseThrow().get("COLUMN_DEFAULT").toString());
        assertEquals(Set.of("user_id", "idempotency_key", "request_hash", "created_at", "deleted"),
                jdbc.queryForList("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user_notification_config_request'")
                        .stream().map(row -> row.get("COLUMN_NAME").toString()).collect(Collectors.toSet()));
        String account = "notifyqa" + UUID.randomUUID().toString().replace("-", "");
        String password = UUID.randomUUID().toString();
        long userId = IdWorker.getId();
        long otherUserId = IdWorker.getId();
        jdbc.update("INSERT INTO app_user(id,login_account,password_hash,nickname,status) VALUES(?,?,?,?,?)",
                userId, account, encoder.encode(password), "通知隔离验收", "ACTIVE");
        String otherAccount = "notifyqa" + UUID.randomUUID().toString().replace("-", "");
        String otherPassword = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO app_user(id,login_account,password_hash,nickname,status) VALUES(?,?,?,?,?)",
                otherUserId, otherAccount, encoder.encode(otherPassword), "通知隔离验收2", "ACTIVE");
        try {
            String publicKey = data(call(HttpMethod.GET, "/api/app/auth/password-key", null, null)).path("publicKey").asText();
            String token = login(publicKey, account, password);
            String otherToken = login(publicKey, otherAccount, otherPassword);
            assertFalse(token.isBlank());
            assertNotEquals(0, call(HttpMethod.GET, "/api/app/user/notification-configs", null, null).path("code").asInt());
            assertEquals(0, data(call(HttpMethod.GET, "/api/app/user/notification-configs", null, token)).size());
            String payload = encrypt(publicKey, "synthetic-bark-key-123");
            var write = Map.of("encryptedKey", payload, "remove", false, "idempotencyKey", "create-12345678");
            assertTrue(data(call(HttpMethod.PUT, "/api/app/user/notification-configs/BARK", write, token)).path("configured").asBoolean());
            assertEquals(0, data(call(HttpMethod.GET, "/api/app/user/notification-configs", null, otherToken)).size());
            assertTrue(data(call(HttpMethod.PUT, "/api/app/user/notification-configs/BARK", write, token)).path("configured").asBoolean());
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM user_notification_config_request WHERE user_id=?", Integer.class, userId));
            String stored = jdbc.queryForObject("SELECT notification_key FROM user_notification_config WHERE user_id=? AND notification_type='BARK'", String.class, userId);
            assertTrue(stored.startsWith("v1:"));
            assertNotEquals("synthetic-bark-key-123", stored);
            assertEquals("synthetic-bark-key-123", keyCipher.decrypt(stored));
            assertEquals("synthetic-bark-key-123", data(call(HttpMethod.GET, "/api/app/user/notification-configs", null, token)).get(0).path("notificationKey").asText());
            HttpHeaders readHeaders = new HttpHeaders();
            readHeaders.set("X-Auth-Token", token);
            assertEquals("no-store", http.exchange("/api/app/user/notification-configs", HttpMethod.GET,
                    new HttpEntity<>(readHeaders), String.class).getHeaders().getFirst("Cache-Control"));
            assertFalse(data(call(HttpMethod.PUT, "/api/app/user/notification-configs/BARK", Map.of("remove", true, "idempotencyKey", "remove-12345678"), token)).path("configured").asBoolean());
            assertEquals(1, jdbc.queryForObject("SELECT deleted FROM user_notification_config WHERE user_id=? AND notification_type='BARK'", Integer.class, userId));
            assertEquals(0, data(call(HttpMethod.GET, "/api/app/user/notification-configs", null, token)).size());
            assertTrue(data(call(HttpMethod.PUT, "/api/app/user/notification-configs/BARK", Map.of("encryptedKey", payload, "remove", false, "idempotencyKey", "restore-12345678"), token)).path("configured").asBoolean());
            assertEquals(0, jdbc.queryForObject("SELECT deleted FROM user_notification_config WHERE user_id=? AND notification_type='BARK'", Integer.class, userId));
            assertTrue(data(call(HttpMethod.PUT, "/api/app/user/notification-configs/PUSHPLUS", Map.of(
                    "encryptedKey", encrypt(publicKey, "synthetic-pushplus-key-456"), "remove", false,
                    "idempotencyKey", "pushplus-12345678"), token)).path("configured").asBoolean());
            var echoed = data(call(HttpMethod.GET, "/api/app/user/notification-configs", null, token));
            assertEquals(2, echoed.size());
            for (JsonNode config : echoed) {
                assertEquals(config.path("notificationType").asText().equals("BARK")
                        ? "synthetic-bark-key-123" : "synthetic-pushplus-key-456", config.path("notificationKey").asText());
            }
            assertEquals(0, data(call(HttpMethod.GET, "/api/app/user/notification-configs", null, otherToken)).size());
            call(HttpMethod.POST, "/api/app/auth/logout", Map.of(), token);
            call(HttpMethod.POST, "/api/app/auth/logout", Map.of(), otherToken);
        } finally {
            jdbc.update("DELETE FROM user_notification_config_request WHERE user_id=?", userId);
            jdbc.update("DELETE FROM user_notification_config WHERE user_id=?", userId);
            jdbc.update("DELETE FROM app_login_log WHERE user_id=?", userId);
            jdbc.update("DELETE FROM app_login_log WHERE user_id=?", otherUserId);
            jdbc.update("DELETE FROM app_user WHERE id=?", userId);
            jdbc.update("DELETE FROM app_user WHERE id=?", otherUserId);
        }
    }

    private String login(String publicKey, String account, String password) throws Exception {
        var captcha = data(call(HttpMethod.GET, "/api/app/auth/captcha", null, null));
        String svg = new String(Base64.getDecoder().decode(captcha.path("image").asText().split(",")[1]), StandardCharsets.UTF_8);
        String answer = svg.substring(svg.lastIndexOf("fill='#278879'>") + "fill='#278879'>".length(), svg.indexOf("</text>"));
        return data(call(HttpMethod.POST, "/api/app/auth/h5/login", Map.of(
                "account", account, "encryptedPassword", encrypt(publicKey, password),
                "captchaId", captcha.path("captchaId").asText(), "captchaCode", answer), null)).path("token").asText();
    }

    private JsonNode call(HttpMethod method, String path, Object payload, String token) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) headers.set("X-Auth-Token", token);
        return json.readTree(http.exchange(path, method, new HttpEntity<>(payload, headers), String.class).getBody());
    }

    private JsonNode data(JsonNode result) {
        assertEquals(0, result.path("code").asInt(-1), "API should succeed");
        return result.path("data");
    }

    private String encrypt(String key, String plain) throws Exception {
        var publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(key)));
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
        return Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
    }
}
