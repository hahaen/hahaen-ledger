package com.hahaen.ledger.transaction.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.hahaen.ledger.account.service.AccountService;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.CurrentUser;
import com.hahaen.ledger.todo.service.TodoReminderWorker;
import com.hahaen.ledger.transaction.dto.RefundRequest;
import com.hahaen.ledger.transaction.dto.TransactionRequest;
import com.hahaen.ledger.transaction.mapper.TransactionDetailMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

/** 显式 DEV 验证真实 MySQL/Mapper/事务；身份为夹具，不替代真实登录。合成数据均回滚。 */
@SpringBootTest(properties = "logging.file.path=target/delete-dev-logs")
@ActiveProfiles("dev")
@EnabledIfSystemProperty(named = "delete.dev.verify", matches = "true")
class LogicalDeleteDevIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired TransactionService transactions;
    @Autowired AccountService accounts;
    @Autowired PlatformTransactionManager manager;
    @MockitoBean TodoReminderWorker worker;
    @MockitoSpyBean TransactionDetailMapper mapper;
    private long fixture() {
        long user = IdWorker.getId();
        jdbc.update("INSERT INTO app_user (id,nickname) VALUES (?,?)", user, "删除回归夹具"); return user;
    }
    private long account(long user, String type) {
        long id = IdWorker.getId();
        jdbc.update("INSERT INTO asset_account (id,user_id,account_name,account_type,balance_cent,total_limit_cent,current_debt_cent) VALUES (?,?,?,?,?,?,?)",
            id, user, "删除回归账户", type, type.equals("FUND") ? 10000L : null,
            type.equals("CREDIT") ? 10000L : null, type.equals("CREDIT") ? 1000L : null); return id;
    }
    private void identity(org.mockito.MockedStatic<CurrentUser> current, long user) {
        current.when(CurrentUser::id).thenReturn(user); current.when(CurrentUser::optionalId).thenReturn(user);
        current.when(CurrentUser::optionalName).thenReturn("删除回归夹具");
    }
    private long bill(String type, long first, long second) {
        boolean single = type.equals("EXPENSE") || type.equals("INCOME");
        return Long.parseLong(transactions.create(new TransactionRequest(type, 100L, single ? first : null,
            single ? null : first, single ? null : second, LocalDateTime.now().toString(), "删除回归", UUID.randomUUID().toString())).id());
    }
    @ParameterizedTest @ValueSource(strings = {"EXPENSE", "INCOME", "TRANSFER", "REPAYMENT"}) @Transactional
    void deletionDisappearsAndRestoresBalancesOnce(String type) {
        long user = fixture(), first = account(user, "FUND"), second = account(user, type.equals("REPAYMENT") ? "CREDIT" : "FUND");
        var before = jdbc.queryForList("SELECT balance_cent,current_debt_cent FROM asset_account WHERE user_id=? ORDER BY id", user);
        try (var current = mockStatic(CurrentUser.class)) {
            identity(current, user); long id = bill(type, first, second);
            if (type.equals("EXPENSE") || type.equals("INCOME")) transactions.createRefund(id, new RefundRequest(25L, UUID.randomUUID().toString()));
            identity(current, fixture()); assertThrows(BusinessException.class, () -> transactions.delete(id));
            identity(current, user); transactions.delete(id);
            var deleted = jdbc.queryForMap("SELECT deleted,deleted_at,deleted_by,deleted_name FROM transaction_detail WHERE id=?", id);
            assertEquals(1, ((Number) deleted.get("deleted")).intValue()); assertNotNull(deleted.get("deleted_at"));
            assertEquals(user, ((Number) deleted.get("deleted_by")).longValue()); assertEquals("删除回归夹具", deleted.get("deleted_name"));
            assertThrows(BusinessException.class, () -> transactions.detail(id));
            assertTrue(transactions.list(null, null, null, null, 1, 50).items().isEmpty());
            assertTrue(mapper.selectByPeriod(user, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)).isEmpty());
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM transaction_refund WHERE transaction_id=? AND deleted=0", Integer.class, id));
            assertThrows(BusinessException.class, () -> transactions.delete(id));
            assertEquals(before, jdbc.queryForList("SELECT balance_cent,current_debt_cent FROM asset_account WHERE user_id=? ORDER BY id", user));
            identity(current, fixture()); assertThrows(BusinessException.class, () -> accounts.delete(first));
            identity(current, user); accounts.delete(first); assertThrows(BusinessException.class, () -> accounts.owned(first, false));
            assertThrows(BusinessException.class, () -> accounts.delete(first));
            assertFalse(accounts.list().stream().anyMatch(value -> value.id().equals(Long.toString(first))));
            assertEquals(1, jdbc.queryForObject("SELECT deleted FROM asset_account WHERE id=?", Integer.class, first));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM transaction_detail WHERE id=?", Integer.class, id));
        }
    }
    @Test void zeroRowsRollsBackWrites() {
        long[] user = new long[1]; doReturn(0).when(mapper).softDeleteById(any());
        try {
            assertThrows(BusinessException.class, () -> new TransactionTemplate(manager).execute(status -> {
                user[0] = fixture(); long first = account(user[0], "FUND");
                try (var current = mockStatic(CurrentUser.class)) {
                    identity(current, user[0]); long id = bill("EXPENSE", first, first);
                    transactions.createRefund(id, new RefundRequest(25L, UUID.randomUUID().toString())); transactions.delete(id);
                }
                return null;
            }));
            for (String table : new String[]{"asset_account", "transaction_detail"})
                assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE user_id=?", Integer.class, user[0]));
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM app_user WHERE id=?", Integer.class, user[0]));
        } finally { reset(mapper); }
    }
}
