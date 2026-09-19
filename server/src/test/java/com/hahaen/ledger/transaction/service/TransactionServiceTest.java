package com.hahaen.ledger.transaction.service;

import com.hahaen.ledger.account.entity.AssetAccount;
import com.hahaen.ledger.account.mapper.AssetAccountMapper;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.CurrentUser;
import com.hahaen.ledger.transaction.dto.RefundRequest;
import com.hahaen.ledger.transaction.dto.TransactionRequest;
import com.hahaen.ledger.transaction.entity.TransactionDetail;
import com.hahaen.ledger.transaction.mapper.TransactionDetailMapper;
import com.hahaen.ledger.transaction.mapper.TransactionRefundMapper;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {
    @Test
    void repaymentEditValidatesRestoredBalanceAndDebt() {
        var transactions = mock(TransactionDetailMapper.class);
        var refunds = mock(TransactionRefundMapper.class);
        var accounts = mock(AssetAccountMapper.class);
        var bill = new TransactionDetail();
        bill.setId(20L); bill.setTransactionType("REPAYMENT"); bill.setFromAccountId(10L); bill.setToAccountId(11L);
        bill.setAmount(500L); bill.setOriginalAmount(500L);
        var fund = fund(10L, 0L);
        var credit = credit(11L, 1000L, 0L);
        when(transactions.selectOwnedForUpdate(20L, 7L)).thenReturn(bill);
        when(accounts.selectOwnedForUpdate(10L, 7L)).thenReturn(fund);
        when(accounts.selectOwnedForUpdate(11L, 7L)).thenReturn(credit);
        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            new TransactionService(transactions, refunds, accounts).update(20L,
                    new TransactionRequest("REPAYMENT", 400L, null, 10L, 11L, "2026-09-08T12:00", "调整还款", null));
            assertEquals(100L, fund.getBalanceCent());
            assertEquals(100L, credit.getCurrentDebtCent());
        }
    }

    @Test
    void concurrentRefundDeletionDoesNotApplyBalanceTwice() {
        var transactions = mock(TransactionDetailMapper.class);
        var refunds = mock(TransactionRefundMapper.class);
        var accounts = mock(AssetAccountMapper.class);
        var refund = new com.hahaen.ledger.transaction.entity.TransactionRefund();
        refund.setId(31L); refund.setTransactionId(20L); refund.setRefundAmount(250L);
        when(refunds.selectActiveById(31L)).thenReturn(refund);
        when(transactions.selectOwnedForUpdate(20L, 7L)).thenReturn(new TransactionDetail());
        when(refunds.selectActiveByIdForUpdate(31L)).thenReturn(null);
        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            assertThrows(BusinessException.class, () -> new TransactionService(transactions, refunds, accounts).deleteRefund(31L));
            verifyNoInteractions(accounts);
            verify(refunds, never()).updateById(any(com.hahaen.ledger.transaction.entity.TransactionRefund.class));
        }
    }

    @Test
    void deleteRefundSoftDeletesAndRestoresExpenseImpact() {
        var transactions = mock(TransactionDetailMapper.class);
        var refunds = mock(TransactionRefundMapper.class);
        var accounts = mock(AssetAccountMapper.class);
        var bill = new TransactionDetail();
        bill.setId(20L);
        bill.setTransactionType("EXPENSE");
        bill.setOriginalAmount(1_000L);
        bill.setAmount(750L);
        bill.setHasRefund(1);
        bill.setAccountId(10L);
        var refund = new com.hahaen.ledger.transaction.entity.TransactionRefund();
        refund.setId(31L);
        refund.setTransactionId(20L);
        refund.setRefundAmount(250L);
        refund.setDeleted(0);
        var fund = fund(10L, 9_250L);
        when(refunds.selectActiveById(31L)).thenReturn(refund);
        when(refunds.selectActiveByIdForUpdate(31L)).thenReturn(refund);
        when(refunds.sumActiveAmount(20L)).thenReturn(0L);
        when(refunds.softDeleteById(refund)).thenReturn(1);
        when(transactions.selectOwnedForUpdate(20L, 7L)).thenReturn(bill);
        when(accounts.selectOwnedForUpdate(10L, 7L)).thenReturn(fund);
        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            new TransactionService(transactions, refunds, accounts).deleteRefund(31L);
            assertEquals(1, refund.getDeleted());
            assertEquals(1_000L, bill.getAmount());
            assertEquals(0, bill.getHasRefund());
            assertEquals(9_000L, fund.getBalanceCent());
            verify(refunds).softDeleteById(refund);
            verify(refunds, never()).updateById(refund);
            verify(transactions).updateById(bill);
            verify(accounts).updateById(fund);
        }
    }

    @Test
    void rejectsRefundIdempotencyKeyWithDifferentAmount() {
        var transactions = mock(TransactionDetailMapper.class);
        var refunds = mock(TransactionRefundMapper.class);
        var accounts = mock(AssetAccountMapper.class);
        var bill = new TransactionDetail(); bill.setTransactionType("EXPENSE");
        var refund = new com.hahaen.ledger.transaction.entity.TransactionRefund();
        refund.setRefundAmount(250L); refund.setDeleted(0);
        when(transactions.selectOwnedForUpdate(20L, 7L)).thenReturn(bill);
        when(refunds.selectByIdempotency(20L, "same-key")).thenReturn(refund);
        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            assertThrows(BusinessException.class, () -> new TransactionService(transactions, refunds, accounts).createRefund(20L, new RefundRequest(300L, "same-key")));
            verifyNoInteractions(accounts);
        }
    }

    @Test
    void editRestoresOldImpactAndKeepsCreationIdempotencyKey() {
        var transactions = mock(TransactionDetailMapper.class);
        var refunds = mock(TransactionRefundMapper.class);
        var accounts = mock(AssetAccountMapper.class);
        var bill = new TransactionDetail();
        bill.setId(20L); bill.setTransactionType("EXPENSE"); bill.setAccountId(10L);
        bill.setAmount(1000L); bill.setOriginalAmount(1000L); bill.setIdempotencyKey("original-key");
        var fund = fund(10L, 9000L);
        when(transactions.selectOwnedForUpdate(20L, 7L)).thenReturn(bill);
        when(accounts.selectOwnedForUpdate(10L, 7L)).thenReturn(fund);
        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            var service = new TransactionService(transactions, refunds, accounts);
            service.update(20L, new TransactionRequest("EXPENSE", 1500L, 10L, null, null, "2026-09-08T12:00", null, "new-key"));
            assertEquals(8500L, fund.getBalanceCent());
            assertEquals("original-key", bill.getIdempotencyKey());
            // 相同编辑重试不会再次累计扣款。
            service.update(20L, new TransactionRequest("EXPENSE", 1500L, 10L, null, null, "2026-09-08T12:00", null, null));
            assertEquals(8500L, fund.getBalanceCent());
        }
    }

    @Test
    void rejectsImpossibleDateBeforeWriting() {
        var transactions = mock(TransactionDetailMapper.class);
        var accounts = mock(AssetAccountMapper.class);
        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            var service = new TransactionService(transactions, mock(TransactionRefundMapper.class), accounts);
            for (String date : new String[] {"2026-02-29T12:00", "0999-01-01T00:00", "2026-09-08T24:00"}) {
                assertThrows(BusinessException.class, () -> service.create(new TransactionRequest("EXPENSE", 100L, 10L, null, null, date, null, null)));
            }
            verifyNoInteractions(accounts);
            verify(transactions, never()).insert(any(TransactionDetail.class));
        }
    }

    @Test
    void expenseAllowsFundBalanceToBecomeNegativeAndPersistsCents() {
        TransactionDetailMapper transactions = mock(TransactionDetailMapper.class);
        TransactionRefundMapper refunds = mock(TransactionRefundMapper.class);
        AssetAccountMapper accounts = mock(AssetAccountMapper.class);
        AssetAccount fund = fund(10L, 100L);
        when(accounts.selectOwnedForUpdate(10L, 7L)).thenReturn(fund);
        doAnswer(invocation -> { TransactionDetail value = invocation.getArgument(0); value.setId(20L); return 1; }).when(transactions).insert(any(TransactionDetail.class));
        TransactionService service = new TransactionService(transactions, refunds, accounts);
        try (MockedStatic<CurrentUser> ignored = mockStatic(CurrentUser.class)) {
            ignored.when(CurrentUser::id).thenReturn(7L);
            var result = service.create(new TransactionRequest("EXPENSE", 1_250L, 10L, null, null,
                    "2026-09-07T10:30", "午餐", "create-1"));
            assertEquals("20", result.id());
            assertEquals("10", result.accountId());
            assertEquals(1_250L, result.amountCents());
            assertEquals(-1_150L, fund.getBalanceCent());
            verify(accounts).updateById(fund);
        }
    }

    @Test
    void allowsRepaymentToMakeFundBalanceNegative() {
        TransactionDetailMapper transactions = mock(TransactionDetailMapper.class);
        TransactionRefundMapper refunds = mock(TransactionRefundMapper.class);
        AssetAccountMapper accounts = mock(AssetAccountMapper.class);
        AssetAccount fund = fund(10L, 100L);
        AssetAccount credit = credit(11L, 1_000L, 500L);
        when(accounts.selectOwnedForUpdate(10L, 7L)).thenReturn(fund);
        when(accounts.selectOwnedForUpdate(11L, 7L)).thenReturn(credit);
        TransactionService service = new TransactionService(transactions, refunds, accounts);
        try (MockedStatic<CurrentUser> ignored = mockStatic(CurrentUser.class)) {
            ignored.when(CurrentUser::id).thenReturn(7L);
            service.create(new TransactionRequest("REPAYMENT", 200L, null,
                    10L, 11L, "2026-09-07T10:30", null, "repay-1"));
            assertEquals(-100L, fund.getBalanceCent());
            assertEquals(300L, credit.getCurrentDebtCent());
            verify(transactions).insert(any(TransactionDetail.class));
        }
    }

    @Test
    void allowsTransferToMakeSourceFundNegative() {
        var transactions = mock(TransactionDetailMapper.class);
        var refunds = mock(TransactionRefundMapper.class);
        var accounts = mock(AssetAccountMapper.class);
        AssetAccount source = fund(10L, 100L);
        AssetAccount target = fund(11L, 300L);
        when(accounts.selectOwnedForUpdate(10L, 7L)).thenReturn(source);
        when(accounts.selectOwnedForUpdate(11L, 7L)).thenReturn(target);
        doAnswer(invocation -> { ((TransactionDetail) invocation.getArgument(0)).setId(20L); return 1; })
                .when(transactions).insert(any(TransactionDetail.class));
        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            new TransactionService(transactions, refunds, accounts).create(new TransactionRequest("TRANSFER", 500L,
                    null, 10L, 11L, "2026-09-07T10:30", null, "transfer-1"));
            assertEquals(-400L, source.getBalanceCent());
            assertEquals(800L, target.getBalanceCent());
        }
    }

    @Test
    void expenseCanExceedCreditAccountLimit() {
        var transactions = mock(TransactionDetailMapper.class);
        var refunds = mock(TransactionRefundMapper.class);
        var accounts = mock(AssetAccountMapper.class);
        AssetAccount credit = credit(11L, 1_000L, 900L);
        when(accounts.selectOwnedForUpdate(11L, 7L)).thenReturn(credit);
        doAnswer(invocation -> { ((TransactionDetail) invocation.getArgument(0)).setId(20L); return 1; })
                .when(transactions).insert(any(TransactionDetail.class));
        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            var service = new TransactionService(transactions, refunds, accounts);
            service.create(new TransactionRequest("EXPENSE", 100L, 11L, null, null,
                    "2026-09-07T10:30", null, "credit-expense-1"));
            assertEquals(1_000L, credit.getCurrentDebtCent());
            service.create(new TransactionRequest("EXPENSE", 1L, 11L, null, null,
                    "2026-09-07T10:31", null, "credit-expense-2"));
            assertEquals(1_001L, credit.getCurrentDebtCent());
        }
    }

    @Test
    void refundReducesEffectiveBillAndRestoresExpenseFundBalance() {
        TransactionDetailMapper transactions = mock(TransactionDetailMapper.class);
        TransactionRefundMapper refunds = mock(TransactionRefundMapper.class);
        AssetAccountMapper accounts = mock(AssetAccountMapper.class);
        TransactionDetail bill = new TransactionDetail();
        bill.setId(20L); bill.setUserId(7L); bill.setTransactionType("EXPENSE"); bill.setOriginalAmount(1_000L); bill.setAmount(1_000L); bill.setHasRefund(0); bill.setAccountId(10L);
        AssetAccount fund = fund(10L, 9_000L);
        when(transactions.selectOwnedForUpdate(20L, 7L)).thenReturn(bill);
        when(refunds.sumActiveAmount(20L)).thenReturn(0L);
        when(accounts.selectOwnedForUpdate(10L, 7L)).thenReturn(fund);
        doAnswer(invocation -> { var value = (com.hahaen.ledger.transaction.entity.TransactionRefund) invocation.getArgument(0); value.setId(31L); value.setCreatedAt(LocalDateTime.now()); return 1; }).when(refunds).insert(any(com.hahaen.ledger.transaction.entity.TransactionRefund.class));
        TransactionService service = new TransactionService(transactions, refunds, accounts);
        try (MockedStatic<CurrentUser> ignored = mockStatic(CurrentUser.class)) {
            ignored.when(CurrentUser::id).thenReturn(7L);
            service.createRefund(20L, new RefundRequest(250L, "refund-1"));
            assertEquals(750L, bill.getAmount());
            assertEquals(1, bill.getHasRefund());
            assertEquals(9_250L, fund.getBalanceCent());
        }
    }

    @Test
    void creditExpenseRefundCanProduceNegativeDebtBalance() {
        var transactions = mock(TransactionDetailMapper.class);
        var refunds = mock(TransactionRefundMapper.class);
        var accounts = mock(AssetAccountMapper.class);
        var bill = new TransactionDetail();
        bill.setId(20L); bill.setUserId(7L); bill.setTransactionType("EXPENSE");
        bill.setOriginalAmount(500L); bill.setAmount(500L); bill.setHasRefund(0); bill.setAccountId(11L);
        AssetAccount credit = credit(11L, 1_000L, 0L);
        var refund = new com.hahaen.ledger.transaction.entity.TransactionRefund();
        when(transactions.selectOwnedForUpdate(20L, 7L)).thenReturn(bill);
        when(refunds.sumActiveAmount(20L)).thenReturn(0L);
        when(accounts.selectOwnedForUpdate(11L, 7L)).thenReturn(credit);
        doAnswer(invocation -> {
            var value = (com.hahaen.ledger.transaction.entity.TransactionRefund) invocation.getArgument(0);
            value.setId(31L); value.setDeleted(0); refund.setId(31L); refund.setTransactionId(20L);
            refund.setRefundAmount(value.getRefundAmount()); refund.setDeleted(0);
            return 1;
        })
                .when(refunds).insert(any(com.hahaen.ledger.transaction.entity.TransactionRefund.class));
        when(refunds.selectActiveById(31L)).thenReturn(refund);
        when(refunds.selectActiveByIdForUpdate(31L)).thenReturn(refund);
        when(refunds.softDeleteById(refund)).thenReturn(1);
        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            new TransactionService(transactions, refunds, accounts).createRefund(20L, new RefundRequest(500L, "credit-refund-1"));
            assertEquals(-500L, credit.getCurrentDebtCent());
            new TransactionService(transactions, refunds, accounts).deleteRefund(31L);
            assertEquals(0L, credit.getCurrentDebtCent());
        }
    }

    @Test
    void doesNotAllowUnknownTransactionToBeAccessed() {
        TransactionDetailMapper transactions = mock(TransactionDetailMapper.class);
        when(transactions.selectOwnedForUpdate(20L, 7L)).thenReturn(null);
        TransactionService service = new TransactionService(transactions, mock(TransactionRefundMapper.class), mock(AssetAccountMapper.class));
        try (MockedStatic<CurrentUser> ignored = mockStatic(CurrentUser.class)) {
            ignored.when(CurrentUser::id).thenReturn(7L);
            assertThrows(BusinessException.class, () -> service.delete(20L));
        }
    }

    private static AssetAccount fund(long id, long balance) {
        AssetAccount value = new AssetAccount(); value.setId(id); value.setUserId(7L); value.setAccountType("FUND"); value.setBalanceCent(balance); value.setDeleted(0); value.setIncludeNetAsset(1); return value;
    }

    private static AssetAccount credit(long id, long limit, long debt) {
        AssetAccount value = new AssetAccount(); value.setId(id); value.setUserId(7L); value.setAccountType("CREDIT"); value.setTotalLimitCent(limit); value.setCurrentDebtCent(debt); value.setDeleted(0); value.setIncludeNetAsset(1); return value;
    }
}
