package com.hahaen.ledger.home.vo;
import com.hahaen.ledger.transaction.entity.LedgerTransaction;
import java.util.List;
public record HomeSummaryVO(String month, long dailyExpenseCents, long expenseCents, long incomeCents, long balanceCents, List<LedgerTransaction> transactions) {}
