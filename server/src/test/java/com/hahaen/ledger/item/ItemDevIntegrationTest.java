package com.hahaen.ledger.item;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.*;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.KeyFactory;
import java.security.spec.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** 显式开启后才连接本机dev依赖；仅创建/清理隔离合成用户和物品，不读取真实账号凭证。 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"logging.file.path=target/item-dev-logs"})
@ActiveProfiles("dev")
@EnabledIfSystemProperty(named = "item.dev.verify", matches = "true")
class ItemDevIntegrationTest {
    @Autowired TestRestTemplate http;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;
    @org.springframework.boot.test.web.server.LocalServerPort int backendPort;
    private final List<Long> userIds = new ArrayList<>();
    private final List<String> sessions = new ArrayList<>();

    private JsonNode call(HttpMethod method, String path, Object payload, String session) throws Exception {
        HttpHeaders headers = new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON);
        if (session != null) headers.set("X-Auth-Token", session);
        var response = http.exchange(path, method, new HttpEntity<>(payload, headers), String.class);
        return json.readTree(response.getBody());
    }
    private JsonNode data(JsonNode result) {
        assertEquals(0, result.path("code").asInt(-1), "业务接口应成功");
        return result.path("data");
    }
    private String login() throws Exception {
        String account = "itemqa" + UUID.randomUUID().toString().replace("-", "");
        String password = UUID.randomUUID().toString();
        long userId = com.baomidou.mybatisplus.core.toolkit.IdWorker.getId();
        jdbc.update("INSERT INTO app_user(id,login_account,password_hash,nickname,status) VALUES(?,?,?,?,?)",
            userId, account, encoder.encode(password), "物品隔离验收", "ACTIVE");
        userIds.add(userId);
        var key = data(call(HttpMethod.GET, "/api/app/auth/password-key", null, null)).path("publicKey").asText();
        var publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(key)));
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
        String encrypted = Base64.getEncoder().encodeToString(cipher.doFinal(password.getBytes(StandardCharsets.UTF_8)));
        var captcha = data(call(HttpMethod.GET, "/api/app/auth/captcha", null, null));
        String svg = new String(Base64.getDecoder().decode(captcha.path("image").asText().split(",")[1]), StandardCharsets.UTF_8);
        String answer = svg.substring(svg.lastIndexOf("fill='#278879'>") + "fill='#278879'>".length(), svg.indexOf("</text>"));
        var login = data(call(HttpMethod.POST, "/api/app/auth/h5/login", Map.of("account", account, "encryptedPassword", encrypted,
            "captchaId", captcha.path("captchaId").asText(), "captchaCode", answer), null));
        String token = login.path("token").asText(); assertFalse(token.isBlank()); sessions.add(token); return token;
    }
    @Test void realFlywaySchemaAuthLifecycleIsolationAndConcurrentRetry() throws Exception {
        try {
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM haji_flyway_history WHERE version='6' AND success=1", Integer.class));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM haji_flyway_history WHERE version='8' AND success=1", Integer.class));
            var reactivateColumns = jdbc.queryForList("SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_TYPE, COLUMN_COMMENT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='personal_item_reactivate_request'");
            assertEquals(Set.of("user_id", "idempotency_key", "item_id", "created_at", "deleted"),
                reactivateColumns.stream().map(column -> Objects.toString(column.get("COLUMN_NAME"))).collect(java.util.stream.Collectors.toSet()));
            for (var column : reactivateColumns) assertFalse(Objects.toString(column.get("COLUMN_COMMENT"), "").isBlank());
            var requestCreated = reactivateColumns.stream().filter(column -> "created_at".equals(column.get("COLUMN_NAME"))).findFirst().orElseThrow();
            assertEquals("NO", requestCreated.get("IS_NULLABLE")); assertEquals("datetime(3)", requestCreated.get("COLUMN_TYPE"));
            assertEquals("CURRENT_TIMESTAMP(3)", requestCreated.get("COLUMN_DEFAULT"));
            var requestDeleted = reactivateColumns.stream().filter(column -> "deleted".equals(column.get("COLUMN_NAME"))).findFirst().orElseThrow();
            assertEquals("YES", requestDeleted.get("IS_NULLABLE")); assertEquals("0", requestDeleted.get("COLUMN_DEFAULT"));
            var columns = jdbc.queryForList("SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_TYPE, COLUMN_COMMENT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='personal_item'");
            assertEquals(22, columns.size());
            for (var column : columns) assertFalse(Objects.toString(column.get("COLUMN_COMMENT"), "").isBlank());
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='personal_item' AND COLUMN_NAME='updated_name'", Integer.class));
            var columnNames = columns.stream().map(column -> Objects.toString(column.get("COLUMN_NAME"))).collect(java.util.stream.Collectors.toSet());
            var entityColumns = new HashSet<String>();
            for (Class<?> type = com.hahaen.ledger.item.entity.PersonalItem.class; type != Object.class; type = type.getSuperclass()) {
                for (var field : type.getDeclaredFields()) {
                    if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) entityColumns.add(field.getName().replaceAll("([A-Z])", "_$1").toLowerCase(Locale.ROOT));
                }
            }
            assertEquals(columnNames, entityColumns);
            var auditNames = List.of("created_at", "created_by", "created_name", "updated_at", "updated_by", "update_name", "deleted_at", "deleted_by", "deleted_name", "deleted");
            for (var column : columns) {
                String name = Objects.toString(column.get("COLUMN_NAME"));
                if (!auditNames.contains(name)) continue;
                assertEquals(name.equals("created_at") ? "NO" : "YES", column.get("IS_NULLABLE"));
                if (name.equals("created_at")) { assertEquals("datetime(3)", column.get("COLUMN_TYPE")); assertEquals("CURRENT_TIMESTAMP(3)", column.get("COLUMN_DEFAULT")); }
                if (name.equals("deleted")) assertEquals("0", column.get("COLUMN_DEFAULT"));
            }
            String owner = login(), stranger = login();
            String today = LocalDate.now(ZoneId.of("Asia/Shanghai")).toString();
            String purchased = LocalDate.now(ZoneId.of("Asia/Shanghai")).minusDays(9).toString();
            String key = "qa_" + UUID.randomUUID().toString().replace("-", "");
            var create = Map.of("name", "隔离验收耳机", "priceCents", 10000, "purchasedOn", purchased, "serving", true, "idempotencyKey", key);
            assertNotEquals(0, call(HttpMethod.GET, "/api/app/items", null, null).path("code").asInt());
            JsonNode assetsBefore = data(call(HttpMethod.GET, "/api/app/assets/overview", null, owner));
            String id;
            try (var executor = Executors.newFixedThreadPool(6)) {
                List<Future<JsonNode>> futures = new ArrayList<>();
                for (int i = 0; i < 6; i++) futures.add(executor.submit(() -> data(call(HttpMethod.POST, "/api/app/items", create, owner))));
                id = futures.getFirst().get().path("id").asText();
                for (var future : futures) assertEquals(id, future.get().path("id").asText());
            }
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM personal_item WHERE user_id=?", Integer.class, userIds.getFirst()));
            var overview = data(call(HttpMethod.GET, "/api/app/items", null, owner));
            assertEquals(10000, overview.path("totalAssetsCents").asLong()); assertEquals(1000, overview.path("totalDailyCostCents").asInt());
            assertEquals(10, overview.path("items").get(0).path("serviceDays").asInt());
            assertEquals(0, data(call(HttpMethod.GET, "/api/app/items", null, stranger)).path("total").asInt());
            assertNotEquals(0, call(HttpMethod.GET, "/api/app/items/" + id, null, stranger).path("code").asInt());
            jdbc.update("UPDATE app_user SET deleted=1 WHERE id=?", userIds.getFirst());
            assertEquals(0, data(call(HttpMethod.GET, "/api/app/items", null, owner)).path("total").asInt());
            assertNotEquals(0, call(HttpMethod.GET, "/api/app/items/" + id, null, owner).path("code").asInt());
            assertNotEquals(0, call(HttpMethod.POST, "/api/app/items", create, owner).path("code").asInt());
            jdbc.update("UPDATE app_user SET deleted=0 WHERE id=?", userIds.getFirst());
            var retire = Map.of("retiredOn", today, "resaleCents", 12000, "idempotencyKey", "retire_" + key);
            assertNotEquals(0, call(HttpMethod.POST, "/api/app/items/" + id + "/retire", retire, stranger).path("code").asInt());
            assertNotEquals(0, call(HttpMethod.DELETE, "/api/app/items/" + id + "?idempotencyKey=" + key, null, stranger).path("code").asInt());
            var invalid = new HashMap<String, Object>(create); invalid.put("purchasedOn", LocalDate.now(ZoneId.of("Asia/Shanghai")).plusDays(1).toString());
            assertNotEquals(0, call(HttpMethod.POST, "/api/app/items", invalid, owner).path("code").asInt());
            var retired = data(call(HttpMethod.POST, "/api/app/items/" + id + "/retire", retire, owner));
            assertEquals(-2000, retired.path("netCostCents").asLong()); assertEquals(-200, retired.path("dailyCostCents").asInt());
            data(call(HttpMethod.POST, "/api/app/items/" + id + "/retire", retire, owner));
            var conflict = new HashMap<String, Object>(retire); conflict.put("resaleCents", 13000);
            assertNotEquals(0, call(HttpMethod.POST, "/api/app/items/" + id + "/retire", conflict, owner).path("code").asInt());
            var detail = data(call(HttpMethod.GET, "/api/app/items/" + id, null, owner));
            assertEquals(10, detail.path("item").path("serviceDays").asInt());
            assertEquals(-200, detail.path("costHistory").get(9).path("dailyCostCents").asInt());
            assertEquals(0, data(call(HttpMethod.GET, "/api/app/items", null, owner)).path("totalAssetsCents").asLong());
            assertEquals(1, data(call(HttpMethod.GET, "/api/app/items?status=RETIRED", null, owner)).path("total").asInt());
            var reactivate = Map.of("idempotencyKey", "reactivate_" + key);
            assertNotEquals(0, call(HttpMethod.POST, "/api/app/items/" + id + "/reactivate", reactivate, stranger).path("code").asInt());
            var activeAgain = data(call(HttpMethod.POST, "/api/app/items/" + id + "/reactivate", reactivate, owner));
            assertEquals("ACTIVE", activeAgain.path("status").asText());
            assertTrue(activeAgain.path("retiredOn").isNull()); assertTrue(activeAgain.path("resaleCents").isNull());
            assertEquals(10000, activeAgain.path("netCostCents").asLong());
            data(call(HttpMethod.POST, "/api/app/items/" + id + "/reactivate", reactivate, owner));
            var stored = jdbc.queryForMap("SELECT status,retired_on,resale_cent FROM personal_item WHERE id=?", Long.valueOf(id));
            assertEquals("ACTIVE", stored.get("status")); assertNull(stored.get("retired_on")); assertNull(stored.get("resale_cent"));
            assertNotEquals(0, call(HttpMethod.POST, "/api/app/items/" + id + "/retire", retire, owner).path("code").asInt());
            assertEquals(10000, data(call(HttpMethod.GET, "/api/app/items", null, owner)).path("totalAssetsCents").asLong());
            var retireAgain = Map.of("retiredOn", today, "resaleCents", 0, "idempotencyKey", "retire_again_" + key);
            data(call(HttpMethod.POST, "/api/app/items/" + id + "/retire", retireAgain, owner));
            assertNotEquals(0, call(HttpMethod.POST, "/api/app/items/" + id + "/reactivate", reactivate, owner).path("code").asInt());
            data(call(HttpMethod.POST, "/api/app/items", create, owner)); // 退役后原始创建重试不重复插入
            data(call(HttpMethod.DELETE, "/api/app/items/" + id + "?idempotencyKey=" + key, null, owner));
            data(call(HttpMethod.DELETE, "/api/app/items/" + id + "?idempotencyKey=" + key, null, owner));
            var audit = jdbc.queryForMap("SELECT deleted,deleted_at,deleted_by,deleted_name,updated_at,created_by,create_hash FROM personal_item WHERE id=?", Long.valueOf(id));
            assertEquals(1, ((Number) audit.get("deleted")).intValue()); assertNotNull(audit.get("deleted_at"));
            assertEquals(userIds.getFirst(), ((Number) audit.get("deleted_by")).longValue()); assertEquals("物品隔离验收", audit.get("deleted_name"));
            assertNotNull(audit.get("created_by")); assertNotNull(audit.get("updated_at"));
            assertNotEquals(0, call(HttpMethod.GET, "/api/app/items/" + id, null, owner).path("code").asInt());
            assertNotEquals(0, call(HttpMethod.POST, "/api/app/items", create, owner).path("code").asInt());
            assertEquals(0, data(call(HttpMethod.GET, "/api/app/items?status=ALL", null, owner)).path("total").asInt());
            assertEquals(assetsBefore, data(call(HttpMethod.GET, "/api/app/assets/overview", null, owner)));
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM transaction_detail WHERE user_id=?", Integer.class, userIds.getFirst()));
            if (Boolean.getBoolean("item.ui.verify")) {
                ItemUiVerifier.run(backendPort, owner);
                assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM personal_item WHERE user_id=? AND deleted=0 AND status='RETIRED'", Integer.class, userIds.getFirst()));
                assertEquals(assetsBefore, data(call(HttpMethod.GET, "/api/app/assets/overview", null, owner)));
                assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM transaction_detail WHERE user_id=?", Integer.class, userIds.getFirst()));
            }
            Path evidence = Path.of(System.getProperty("basedir"), "../docs/10-iterations/2026/09/item-reactivation/evidence/dev-api.json");
            Files.createDirectories(evidence.getParent());
            json.writerWithDefaultPrettyPrinter().writeValue(evidence.toFile(), Map.of("status", "PASS", "date", today,
                "checks", List.of("Flyway V6和V8", "22列中文注释", "V8关系表列名注释默认值", "真实RSA登录", "未登录拒绝", "6路并发创建幂等", "跨用户读写隔离", "逻辑删除用户无物品访问权", "退役冻结与负成本", "重新服役清除退役日期与二手价格", "重新服役重试与状态冲突", "软删除审计", "记账账户零影响", "Entity与实际列一致", "公共字段默认值与可空规则"), "columns", columns, "reactivateRequestColumns", reactivateColumns));
        } finally {
            for (String token : sessions) call(HttpMethod.POST, "/api/app/auth/logout", null, token);
            for (Long id : userIds) {
                jdbc.update("DELETE FROM personal_item_reactivate_request WHERE user_id=?", id);
                jdbc.update("DELETE FROM personal_item WHERE user_id=?", id);
                jdbc.update("DELETE FROM app_login_log WHERE user_id=?", id);
                jdbc.update("DELETE FROM app_user WHERE id=?", id);
            }
        }
    }
}
