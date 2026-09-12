package com.hahaen.ledger.common.config;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationIntegrityTest {
    @Test
    void baselineMigrationsContainConsolidatedSchema() throws Exception {
        for (String migration : List.of(
                "V1__init_schema.sql",
                "V2__create_app_file_table.sql",
                "V3__create_asset_account_table.sql",
                "V4__create_transaction_detail_and_refund_tables.sql")) {
            String content = readMigration(migration);
            assertTrue(content.contains("DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),
                    migration + " 未声明 utf8mb4_general_ci");
        }

        String userMigration = readMigration("V1__init_schema.sql");
        assertTrue(userMigration.contains("avatar_file_url VARCHAR(512)"));
        assertFalse(userMigration.contains("avatar_file_id"));

        String fileMigration = readMigration("V2__create_app_file_table.sql");
        assertTrue(fileMigration.contains("idx_app_file_user_avatar_hash"));
        assertFalse(fileMigration.contains("fk_app_user_avatar_file"));

        String accountMigration = readMigration("V3__create_asset_account_table.sql");
        assertTrue(accountMigration.contains("sort_order INT NOT NULL DEFAULT 0"));
        assertTrue(accountMigration.contains("idx_asset_account_user_type_deleted_order"));
    }

    private String readMigration(String migration) throws Exception {
        var resource = getClass().getResourceAsStream("/db/migration/" + migration);
        assertNotNull(resource, migration + " 不存在");
        StringBuilder content = new StringBuilder();
        try (var reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
        }
        return content.toString();
    }
}
