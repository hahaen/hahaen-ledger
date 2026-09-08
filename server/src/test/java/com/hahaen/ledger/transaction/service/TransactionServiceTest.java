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
    void expenseDecreasesFundBalanceAndPersistsEffectiveAmountInCents() {
        TransactionDetailMapper transactions = mock(TransactionDetailMapper.class);
        TransactionRefundMapper refunds = mock(TransactionRefundMapper.class);
        AssetAccountMapper accounts = mock(AssetAccountMapper.class);
        AssetAccount fund = fund(10L, 10_000L);
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
            assertEquals(8_750L, fund.getBalanceCent());
            verify(accounts).updateById(fund);
        }
    }

    @Test
    void rejectsRepaymentWhenFundBalanceIsInsufficient() {
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
            assertThrows(BusinessException.class, () -> service.create(new TransactionRequest("REPAYMENT", 101L, null,
                    10L, 11L, "2026-09-07T10:30", null, "repay-1")));
            verify(transactions, never()).insert(any(TransactionDetail.class));
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
