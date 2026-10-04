package com.hahaen.ledger.common.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.hahaen.ledger.account.entity.AssetAccount;
import com.hahaen.ledger.account.mapper.AssetAccountMapper;
import com.hahaen.ledger.transaction.entity.TransactionDetail;
import com.hahaen.ledger.transaction.mapper.TransactionDetailMapper;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/** 使用真实 Mapper SQL 注入器，避免 Mockito 掩盖 @TableLogic 的更新排除行为。 */
class LogicalDeleteSqlTest {
    @Test void transactionDeletionWritesFlagAndRestrictsOwner() {
        var row = new TransactionDetail(); row.setId(1L); row.setUserId(7L); row.setDeleted(1);
        assertDeleteSql(TransactionDetailMapper.class, Map.of("transaction", row));
    }
    @Test void accountDeletionWritesFlagAndRestrictsOwner() {
        var row = new AssetAccount(); row.setId(1L); row.setUserId(7L); row.setDeleted(1);
        assertDeleteSql(AssetAccountMapper.class, Map.of("account", row));
    }
    private void assertDeleteSql(Class<?> mapper, Map<String, Object> parameters) {
        var config = new MybatisConfiguration(); config.addMapper(mapper);
        var bound = config.getMappedStatement(mapper.getName() + ".softDeleteById").getBoundSql(parameters);
        var sql = bound.getSql().replaceAll("\\s+", " ");
        String set = sql.substring(sql.indexOf(" SET "), sql.indexOf(" WHERE "));
        assertTrue(set.contains("deleted = 1"), sql);
        for (String field : new String[]{"deleted_at", "deleted_by", "deleted_name", "updated_at", "updated_by", "update_name"}) assertTrue(set.contains(field + " ="), sql);
        assertTrue(sql.endsWith("WHERE id = ? AND user_id = ? AND deleted = 0"), sql);
    }
}
