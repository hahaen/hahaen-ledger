package com.hahaen.ledger.common.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.FileSystemResource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/** 显式启用才执行：迁移本地配置的 haji_dev，临时业务行全部回滚，禁止输出连接凭证。 */
@EnabledIfSystemProperty(named = "ledger.audit.dev", matches = "true")
class AuditFieldDefaultsDevTest {
    private static final List<String> TABLES = List.of("app_user", "user_identity", "ledger_book",
            "ledger_account", "ledger_transaction", "transaction_refund");
    private static final Map<String, String> COMMENTS = Map.of(
            "created_at", "创建时间", "created_by", "创建人ID", "created_name", "创建人",
            "updated_at", "更新时间", "updated_by", "更新人ID", "update_name", "更新人",
            "deleted_at", "删除时间", "deleted_by", "删除人ID", "deleted_name", "删除人",
            "deleted", "删除标识，0存在1删除");
    private static final Map<String, String> TABLE_COMMENTS = Map.of(
            "app_user", "应用用户表", "user_identity", "用户第三方身份关联表", "ledger_book", "账本表",
            "ledger_account", "账本账户表", "ledger_transaction", "账单流水表", "transaction_refund", "账单退款记录表");
    private static final Map<String, Integer> COLUMN_COUNTS = Map.of(
            "app_user", 13, "user_identity", 8, "ledger_book", 16,
            "ledger_account", 18, "ledger_transaction", 21, "transaction_refund", 16);
    private static final Map<String, Set<String>> INDEXES = Map.of(
            "app_user", Set.of("PRIMARY"),
            "user_identity", Set.of("PRIMARY", "uk_identity_provider_open_id", "idx_identity_user"),
            "ledger_book", Set.of("PRIMARY", "uk_book_user_name", "idx_book_user_status"),
            "ledger_account", Set.of("PRIMARY", "uk_account_book_name", "idx_account_book_kind_status"),
            "ledger_transaction", Set.of("PRIMARY", "uk_transaction_idempotency", "idx_transaction_book_occurred",
                    "idx_transaction_account", "idx_transaction_from", "idx_transaction_to"),
            "transaction_refund", Set.of("PRIMARY", "uk_refund_idempotency", "idx_refund_transaction_status"));
    private static final Set<String> FOREIGN_KEYS = Set.of("fk_identity_user", "fk_book_user", "fk_account_book",
            "fk_transaction_book", "fk_transaction_account", "fk_transaction_from_account",
            "fk_transaction_to_account", "fk_refund_transaction");
    private static final Set<String> CHECKS = Set.of("ck_account_kind", "ck_account_balance", "ck_account_credit_limit",
            "ck_transaction_amount", "ck_transaction_type", "ck_refund_amount");

    @Test
    void migrateAndVerifyDevSchemaAndDefaultInserts() throws Exception {
        var yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new FileSystemResource("src/main/resources/application-dev.yml"));
        var environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new PropertiesPropertySource("local-dev", yaml.getObject()));
        String url = environment.getRequiredProperty("spring.datasource.url");
        String username = environment.getRequiredProperty("spring.datasource.username");
        String password = environment.getRequiredProperty("spring.datasource.password");
        Logger logger = (Logger) LoggerFactory.getLogger("org.flywaydb");
        Level oldLevel = logger.getLevel();
        logger.setLevel(Level.OFF);
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            // 先验证真实目标，不能对生产或其他数据库运行迁移。
            assertEquals("haji_dev", connection.getCatalog(), "仅允许本地 DEV 目标");
            boolean reset = Boolean.getBoolean("ledger.dev.reset");
            var flyway = Flyway.configure().dataSource(url, username, password)
                    .locations("classpath:db/migration").cleanDisabled(!reset).load();
            if (reset) {
                // 仅允许在已断言为 haji_dev 的 DEV 数据库执行一次性重建。
                flyway.clean();
            }
            flyway.migrate();
            flyway.validate();
            assertEquals("1", flyway.info().current().getVersion().getVersion());
            verifySchema(connection);
            verifyColumns(connection);
            verifyInsertsAndRollback(connection);
            System.out.println("DEV 基线验证 PASS：Flyway V1、六表公共列默认值/可空/注释、默认插入、显式 NULL、创建时间非空、事务回滚。");
        } catch (SQLException exception) {
            fail("DEV 数据库验证失败，SQLState=" + exception.getSQLState() + "，错误码=" + exception.getErrorCode());
        } catch (org.flywaydb.core.api.FlywayException exception) {
            fail("DEV Flyway 验证失败：" + exception.getMessage(), exception);
        } finally {
            logger.setLevel(oldLevel);
        }
    }

    private void verifySchema(Connection connection) throws SQLException {
        try (var query = connection.prepareStatement("""
                SELECT table_name, table_comment
                FROM information_schema.tables
                WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'
                AND table_name IN (?, ?, ?, ?, ?, ?)
                """)) {
            int index = 1;
            for (String table : TABLES) query.setString(index++, table);
            try (var rows = query.executeQuery()) {
                Map<String, String> actual = new java.util.HashMap<>();
                while (rows.next()) actual.put(rows.getString(1), rows.getString(2));
                assertEquals(TABLE_COMMENTS, actual, "业务表及表 COMMENT");
            }
        }
        for (String table : TABLES) {
            try (var query = connection.prepareStatement("""
                    SELECT COUNT(*), SUM(column_comment = '')
                    FROM information_schema.columns
                    WHERE table_schema = DATABASE() AND table_name = ?
                    """)) {
                query.setString(1, table);
                try (var rows = query.executeQuery()) {
                    assertTrue(rows.next());
                    assertEquals(COLUMN_COUNTS.get(table), rows.getInt(1), table + " 字段数");
                    assertEquals(0, rows.getInt(2), table + " 字段 COMMENT");
                }
            }
            try (var query = connection.prepareStatement("""
                    SELECT DISTINCT index_name FROM information_schema.statistics
                    WHERE table_schema = DATABASE() AND table_name = ?
                    """)) {
                query.setString(1, table);
                try (var rows = query.executeQuery()) {
                    Set<String> actual = new java.util.HashSet<>();
                    while (rows.next()) actual.add(rows.getString(1));
                    assertEquals(INDEXES.get(table), actual, table + " 索引");
                }
            }
        }
        assertConstraintNames(connection, "FOREIGN KEY", FOREIGN_KEYS);
        assertConstraintNames(connection, "CHECK", CHECKS);
        try (var query = connection.prepareStatement("""
                SELECT version, success FROM flyway_schema_history
                WHERE type = 'SQL' ORDER BY installed_rank
                """)) {
            try (var rows = query.executeQuery()) {
                assertTrue(rows.next());
                assertEquals("1", rows.getString("version"));
                assertTrue(rows.getBoolean("success"));
                assertFalse(rows.next(), "当前基线只能有一个 SQL Migration");
            }
        }
    }

    private void assertConstraintNames(Connection connection, String type, Set<String> expected) throws SQLException {
        try (var query = connection.prepareStatement("""
                SELECT constraint_name FROM information_schema.table_constraints
                WHERE constraint_schema = DATABASE() AND constraint_type = ?
                AND table_name IN (?, ?, ?, ?, ?, ?)
                """)) {
            query.setString(1, type);
            int index = 2;
            for (String table : TABLES) query.setString(index++, table);
            try (var rows = query.executeQuery()) {
                Set<String> actual = new java.util.HashSet<>();
                while (rows.next()) actual.add(rows.getString(1));
                assertEquals(expected, actual, type + " 约束");
            }
        }
    }

    private void verifyColumns(Connection connection) throws SQLException {
        for (String table : TABLES) {
            int auditColumns = 0;
            try (var query = connection.prepareStatement("""
                    SELECT column_name, is_nullable, column_default, column_comment
                    FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ?
                    """)) {
                query.setString(1, table);
                try (var rows = query.executeQuery()) {
                    while (rows.next()) {
                        String column = rows.getString("column_name");
                        if (!COMMENTS.containsKey(column)) continue;
                        auditColumns++;
                        String label = table + "." + column;
                        assertEquals(COMMENTS.get(column), rows.getString("column_comment"), label);
                        assertEquals(column.equals("created_at") ? "NO" : "YES", rows.getString("is_nullable"), label);
                        String value = rows.getString("column_default");
                        if (column.equals("created_at")) {
                            assertNotNull(value, label);
                            assertEquals("CURRENT_TIMESTAMP(3)", value.toUpperCase(java.util.Locale.ROOT), label);
                        } else if (column.equals("deleted")) {
                            assertEquals("0", value, label);
                        } else {
                            assertNull(value, label);
                        }
                    }
                }
            }
            // 关联表保留历史 updated_at 列，但实体仍仅含两个公共字段。
            assertEquals(table.equals("user_identity") ? 3 : 10, auditColumns, table);
        }
    }

    private void verifyInsertsAndRollback(Connection connection) throws SQLException {
        long id = -ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        connection.setAutoCommit(false);
        try (var statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO app_user (id) VALUES (" + id + ")");
            statement.executeUpdate("INSERT INTO user_identity (id,user_id,provider,open_id) VALUES ("
                    + id + "," + id + ",'AUDIT_TEST','" + id + "')");
            statement.executeUpdate("INSERT INTO ledger_book (id,user_id,name) VALUES (" + id + "," + id + ",'审计回归')");
            statement.executeUpdate("INSERT INTO ledger_account (id,book_id,name,kind) VALUES (" + id + "," + id + ",'审计回归','FUND')");
            statement.executeUpdate("INSERT INTO ledger_transaction (id,book_id,type,amount_cents,account_id,occurred_at) VALUES ("
                    + id + "," + id + ",'EXPENSE',1," + id + ",CURRENT_TIMESTAMP(3))");
            statement.executeUpdate("INSERT INTO transaction_refund (id,transaction_id,amount_cents,refunded_at) VALUES ("
                    + id + "," + id + ",1,CURRENT_TIMESTAMP(3))");
            for (String table : TABLES) {
                try (var row = statement.executeQuery("SELECT *, ABS(TIMESTAMPDIFF(SECOND,created_at,CURRENT_TIMESTAMP(3))) AS age_seconds FROM " + table + " WHERE id=" + id)) {
                    assertTrue(row.next(), table);
                    assertNotNull(row.getTimestamp("created_at"), table);
                    assertTrue(row.getLong("age_seconds") < 60, table);
                    assertEquals(0, row.getInt("deleted"), table);
                    assertFalse(row.wasNull(), table);
                    assertNull(row.getObject("updated_at"), table);
                    if (!table.equals("user_identity")) assertNull(row.getObject("created_by"), table);
                }
                statement.executeUpdate("UPDATE " + table + " SET deleted=NULL WHERE id=" + id);
                try (var row = statement.executeQuery("SELECT deleted FROM " + table + " WHERE id=" + id)) {
                    assertTrue(row.next());
                    assertNull(row.getObject(1), table);
                }
            }
            SQLException rejected = assertThrows(SQLException.class,
                    () -> statement.executeUpdate("UPDATE app_user SET created_at=NULL WHERE id=" + id));
            assertEquals("23000", rejected.getSQLState());
        } finally {
            connection.rollback();
            connection.setAutoCommit(true);
        }
        for (String table : TABLES) {
            try (var statement = connection.createStatement();
                 var row = statement.executeQuery("SELECT COUNT(*) FROM " + table + " WHERE id=" + id)) {
                assertTrue(row.next());
                assertEquals(0, row.getInt(1), "测试行必须回滚：" + table);
            }
        }
    }
}
