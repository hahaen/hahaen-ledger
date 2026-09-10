package com.hahaen.ledger.common.config;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MigrationIntegrityTest {
    @Test
    void v5MatchesPreviouslyAppliedMigrationChecksum() throws Exception {
        // 历史迁移必须保持已执行内容；新结构变更应另建迁移。
        var resource = getClass().getResourceAsStream("/db/migration/V5__add_asset_account_sort_order.sql");
        assertNotNull(resource);
        CRC32 checksum = new CRC32();
        try (var reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first && line.startsWith("\uFEFF")) line = line.substring(1);
                first = false;
                checksum.update(line.getBytes(StandardCharsets.UTF_8));
            }
        }
        assertEquals(266153221, (int) checksum.getValue(), "V5 与已执行的数据库迁移不一致");
    }
}
