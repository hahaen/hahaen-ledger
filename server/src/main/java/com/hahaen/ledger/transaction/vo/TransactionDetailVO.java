package com.hahaen.ledger.transaction.vo;
import com.hahaen.ledger.transaction.entity.LedgerTransaction;
import com.hahaen.ledger.transaction.entity.TransactionRefund;
import java.util.List;
public record TransactionDetailVO(LedgerTransaction transaction, long refundedCents, long effectiveCents, List<TransactionRefund> refunds) {}
