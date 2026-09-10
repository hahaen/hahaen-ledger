package com.hahaen.ledger.transaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hahaen.ledger.account.entity.AssetAccount;
import com.hahaen.ledger.account.mapper.AssetAccountMapper;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.AuditSupport;
import com.hahaen.ledger.common.security.CurrentUser;
import com.hahaen.ledger.transaction.dto.RefundRequest;
import com.hahaen.ledger.transaction.dto.RepaymentRequest;
import com.hahaen.ledger.transaction.dto.TransactionRequest;
import com.hahaen.ledger.transaction.entity.TransactionDetail;
import com.hahaen.ledger.transaction.entity.TransactionRefund;
import com.hahaen.ledger.transaction.mapper.TransactionDetailMapper;
import com.hahaen.ledger.transaction.mapper.TransactionRefundMapper;
import com.hahaen.ledger.transaction.vo.RefundVO;
import com.hahaen.ledger.transaction.vo.TransactionDetailVO;
import com.hahaen.ledger.transaction.vo.TransactionPageVO;
import com.hahaen.ledger.transaction.vo.TransactionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    public static final long MAX_CENTS = 99_999_999_999L;
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final Set<String> TYPES = Set.of("EXPENSE", "INCOME", "TRANSFER", "REPAYMENT");

    private final TransactionDetailMapper transactionMapper;
    private final TransactionRefundMapper refundMapper;
    private final AssetAccountMapper accountMapper;

    public TransactionPageVO list(String month, String date, Long accountId, String type, int page, int pageSize) {
        long userId = CurrentUser.id();
        Scope scope = parseScope(month, date);
        if (accountId != null) ownedAccount(accountId, userId, false);
        String normalizedType = normalizeOptionalType(type);
        int safePage = Math.max(1, page);
        int safePageSize = Math.max(1, Math.min(100, pageSize));
        int offset = Math.multiplyExact(safePage - 1, safePageSize);
        List<TransactionDetail> rows = transactionMapper.selectPageByScope(userId, scope.start(), scope.end(), null,
                accountId, normalizedType, safePageSize, offset);
        long total = transactionMapper.countByScope(userId, scope.start(), scope.end(), null, accountId, normalizedType);
        return new TransactionPageVO(rows.stream().map(TransactionService::toVO).toList(), total, safePage, safePageSize);
    }

    public List<TransactionDetail> activeInMonth(long userId, YearMonth month) {
        return transactionMapper.selectByPeriod(userId, month.atDay(1).atStartOfDay(),
                month.plusMonths(1).atDay(1).atStartOfDay());
    }

    @Transactional
    public TransactionVO create(TransactionRequest request) {
        long userId = CurrentUser.id();
        String type = normalizeType(request.type());
        long amount = validAmount(request.amountCents());
        String idempotencyKey = normalizeIdempotencyKey(request.idempotencyKey());
        if (idempotencyKey != null) {
            TransactionDetail existing = transactionMapper.selectByIdempotency(userId, idempotencyKey);
            if (existing != null) {
                if (Integer.valueOf(1).equals(existing.getDeleted())) {
                    throw new BusinessException("IDEMPOTENCY_CONFLICT", "幂等键已被已删除账单占用");
                }
                if (!sameRequest(existing, type, amount, request)) {
                    throw new BusinessException("IDEMPOTENCY_CONFLICT", "幂等键已用于其他账单");
                }
                return toVO(existing);
            }
        }
        LocalDateTime occurredAt = parseOccurredAt(request.occurredAt());
        Map<Long, AssetAccount> accounts = lockAccounts(userId, requestAccountIds(type, request));
        validateAccountShape(type, request, accounts);
        TransactionDetail transaction = new TransactionDetail();
        transaction.setUserId(userId);
        transaction.setTransactionNo("TRX-" + UUID.randomUUID());
        transaction.setTransactionType(type);
        transaction.setOriginalAmount(amount);
        transaction.setAmount(amount);
        transaction.setHasRefund(0);
        transaction.setAccountId(request.accountId());
        transaction.setFromAccountId(request.fromAccountId());
        transaction.setToAccountId(request.toAccountId());
        transaction.setOccurredAt(occurredAt);
        transaction.setNote(validNote(request.note()));
        transaction.setIdempotencyKey(idempotencyKey);
        transactionMapper.insert(transaction);
        applyImpact(transaction, amount, accounts, 1);
        return toVO(transaction);
    }

    @Transactional
    public TransactionVO update(long id, TransactionRequest request) {
        long userId = CurrentUser.id();
        TransactionDetail transaction = transactionMapper.selectOwnedForUpdate(id, userId);
        if (transaction == null) throw new BusinessException("TRANSACTION_NOT_FOUND", "账单不存在");
        String type = normalizeType(request.type());
        long amount = validAmount(request.amountCents());
        long refunded = refundMapper.sumActiveAmount(id);
        if (amount < refunded) throw new BusinessException("REFUND_AMOUNT_INVALID", "原始金额不能小于已退款金额");
        if (refunded > 0 && !sameAccountShape(transaction, type, request)) {
            throw new BusinessException("REFUNDED_TRANSACTION_IMMUTABLE", "已有退款的账单不能修改类型或账户");
        }
        Map<Long, AssetAccount> accounts = lockAccounts(userId, unionAccountIds(transaction, type, request));
        applyImpact(transaction, transaction.getAmount(), accounts, -1);
        // 新还款金额基于撤销旧流水后的余额和欠款校验。
        validateAccountShape(type, request, accounts);
        transaction.setTransactionType(type);
        transaction.setOriginalAmount(amount);
        transaction.setAmount(amount - refunded);
        transaction.setHasRefund(refunded > 0 || Integer.valueOf(1).equals(transaction.getHasRefund()) ? 1 : 0);
        transaction.setAccountId(request.accountId());
        transaction.setFromAccountId(request.fromAccountId());
        transaction.setToAccountId(request.toAccountId());
        transaction.setOccurredAt(parseOccurredAt(request.occurredAt()));
        transaction.setNote(validNote(request.note()));
        // 创建幂等键标识原始创建请求，编辑不能覆盖它。
        transactionMapper.updateById(transaction);
        applyImpact(transaction, transaction.getAmount(), accounts, 1);
        return toVO(transaction);
    }

    @Transactional
    public void delete(long id) {
        long userId = CurrentUser.id();
        TransactionDetail transaction = transactionMapper.selectOwnedForUpdate(id, userId);
        if (transaction == null) throw new BusinessException("TRANSACTION_NOT_FOUND", "账单不存在");
        Map<Long, AssetAccount> accounts = lockAccounts(userId, idsOf(transaction));
        applyImpact(transaction, transaction.getAmount(), accounts, -1);
        for (TransactionRefund refund : refundMapper.selectActiveByTransaction(id)) {
            AuditSupport.markDeleted(refund);
            refundMapper.updateById(refund);
        }
        AuditSupport.markDeleted(transaction);
        transactionMapper.updateById(transaction);
    }

    public TransactionDetailVO detail(long id) {
        long userId = CurrentUser.id();
        TransactionDetail transaction = transactionMapper.selectOne(new LambdaQueryWrapper<TransactionDetail>()
                .eq(TransactionDetail::getId, id)
                .eq(TransactionDetail::getUserId, userId)
                .eq(TransactionDetail::getDeleted, 0));
        if (transaction == null) throw new BusinessException("TRANSACTION_NOT_FOUND", "账单不存在");
        List<TransactionRefund> refunds = refundMapper.selectActiveByTransaction(id);
        long refunded = refunds.stream().mapToLong(TransactionRefund::getRefundAmount).sum();
        return new TransactionDetailVO(toVO(transaction), refunded, transaction.getAmount(), refunds.stream().map(TransactionService::toRefundVO).toList());
    }

    @Transactional
    public RefundVO createRefund(long transactionId, RefundRequest request) {
        long userId = CurrentUser.id();
        TransactionDetail transaction = transactionMapper.selectOwnedForUpdate(transactionId, userId);
        if (transaction == null) throw new BusinessException("TRANSACTION_NOT_FOUND", "账单不存在");
        if (!"EXPENSE".equals(transaction.getTransactionType()) && !"INCOME".equals(transaction.getTransactionType())) {
            throw new BusinessException("REFUND_NOT_SUPPORTED", "转账和还款不支持退款");
        }
        long amount = validAmount(request.amountCents());
        String key = normalizeIdempotencyKey(request.idempotencyKey());
        if (key != null) {
            TransactionRefund existing = refundMapper.selectByIdempotency(transactionId, key);
            if (existing != null) {
                if (Integer.valueOf(1).equals(existing.getDeleted())) throw new BusinessException("IDEMPOTENCY_CONFLICT", "幂等键已被已删除退款占用");
                if (existing.getRefundAmount() != amount) throw new BusinessException("IDEMPOTENCY_CONFLICT", "幂等键已用于其他退款金额");
                return toRefundVO(existing);
            }
        }
        long activeRefunded = refundMapper.sumActiveAmount(transactionId);
        long remaining = transaction.getOriginalAmount() - activeRefunded;
        if (amount > remaining) throw new BusinessException("REFUND_EXCEEDS_REMAINING", "退款金额不能超过剩余可退款金额");
        Map<Long, AssetAccount> accounts = lockAccounts(userId, idsOf(transaction));
        AssetAccount account = account(accounts, transaction.getAccountId());
        requireFund(account);
        TransactionRefund refund = new TransactionRefund();
        refund.setTransactionId(transactionId);
        refund.setRefundNo("REF-" + UUID.randomUUID());
        refund.setRefundAmount(amount);
        refund.setIdempotencyKey(key);
        refundMapper.insert(refund);
        transaction.setAmount(transaction.getOriginalAmount() - activeRefunded - amount);
        transaction.setHasRefund(1);
        transactionMapper.updateById(transaction);
        adjustFund(account, "EXPENSE".equals(transaction.getTransactionType()) ? amount : -amount);
        accountMapper.updateById(account);
        return toRefundVO(refund);
    }

    @Transactional
    public void deleteRefund(long refundId) {
        long userId = CurrentUser.id();
        TransactionRefund refund = refundMapper.selectActiveById(refundId);
        if (refund == null) throw new BusinessException("REFUND_NOT_FOUND", "退款记录不存在");
        TransactionDetail transaction = transactionMapper.selectOwnedForUpdate(refund.getTransactionId(), userId);
        if (transaction == null) throw new BusinessException("REFUND_NOT_FOUND", "退款记录不存在");
        // 等待原账单锁期间，另一请求可能已经删除退款；锁内重新读取避免重复扣款。
        refund = refundMapper.selectActiveByIdForUpdate(refundId);
        if (refund == null) throw new BusinessException("REFUND_NOT_FOUND", "退款记录不存在");
        Map<Long, AssetAccount> accounts = lockAccounts(userId, idsOf(transaction));
        AssetAccount account = account(accounts, transaction.getAccountId());
        requireFund(account);
        AuditSupport.markDeleted(refund);
        if (refundMapper.updateById(refund) != 1) {
            throw new BusinessException("REFUND_NOT_FOUND", "退款记录不存在或已被删除");
        }
        long activeRefunded = refundMapper.sumActiveAmount(transaction.getId());
        transaction.setAmount(transaction.getOriginalAmount() - activeRefunded);
        transaction.setHasRefund(1);
        transactionMapper.updateById(transaction);
        adjustFund(account, "EXPENSE".equals(transaction.getTransactionType()) ? -refund.getRefundAmount() : refund.getRefundAmount());
        accountMapper.updateById(account);
    }

    public TransactionVO repay(long creditAccountId, RepaymentRequest request) {
        return create(new TransactionRequest("REPAYMENT", request.amountCents(), null,
                request.fundAccountId(), creditAccountId, LocalDateTime.now().format(DATE_TIME), null,
                request.idempotencyKey()));
    }

    private Map<Long, AssetAccount> lockAccounts(long userId, Set<Long> ids) {
        Map<Long, AssetAccount> result = new LinkedHashMap<>();
        for (Long id : new TreeSet<>(ids)) {
            if (id == null) continue;
            AssetAccount account = accountMapper.selectOwnedForUpdate(id, userId);
            if (account == null) throw new BusinessException("ACCOUNT_NOT_FOUND", "账户不存在");
            result.put(id, account);
        }
        return result;
    }

    private void validateAccountShape(String type, TransactionRequest request, Map<Long, AssetAccount> accounts) {
        switch (type) {
            case "EXPENSE", "INCOME" -> {
                if (request.accountId() == null || request.fromAccountId() != null || request.toAccountId() != null) {
                    throw new BusinessException("TRANSACTION_ACCOUNT_INVALID", "支出和收入必须选择一个资金账户");
                }
                requireFund(account(accounts, request.accountId()));
            }
            case "TRANSFER" -> {
                if (request.accountId() != null || request.fromAccountId() == null || request.toAccountId() == null
                        || request.fromAccountId().equals(request.toAccountId())) {
                    throw new BusinessException("TRANSFER_ACCOUNT_INVALID", "转账必须选择两个不同的资金账户");
                }
                requireFund(account(accounts, request.fromAccountId()));
                requireFund(account(accounts, request.toAccountId()));
            }
            case "REPAYMENT" -> {
                if (request.accountId() != null || request.fromAccountId() == null || request.toAccountId() == null
                        || request.fromAccountId().equals(request.toAccountId())) {
                    throw new BusinessException("REPAYMENT_ACCOUNT_INVALID", "还款必须选择资金账户和信贷账户");
                }
                requireFund(account(accounts, request.fromAccountId()));
                requireCredit(account(accounts, request.toAccountId()));
                AssetAccount fund = account(accounts, request.fromAccountId());
                AssetAccount credit = account(accounts, request.toAccountId());
                if (value(fund.getBalanceCent()) < request.amountCents()) throw new BusinessException("BALANCE_NOT_ENOUGH", "资金账户余额不足");
                if (value(credit.getCurrentDebtCent()) < request.amountCents()) throw new BusinessException("DEBT_NOT_ENOUGH", "还款金额不能超过当前欠款");
            }
            default -> throw new BusinessException("TRANSACTION_TYPE_INVALID", "账单类型不支持");
        }
    }

    private void applyImpact(TransactionDetail transaction, long amount, Map<Long, AssetAccount> accounts, int multiplier) {
        if (amount < 0) throw new BusinessException("INVALID_AMOUNT", "金额不能为负数");
        switch (transaction.getTransactionType()) {
            case "EXPENSE" -> adjustFund(account(accounts, transaction.getAccountId()), -amount * multiplier);
            case "INCOME" -> adjustFund(account(accounts, transaction.getAccountId()), amount * multiplier);
            case "TRANSFER" -> {
                adjustFund(account(accounts, transaction.getFromAccountId()), -amount * multiplier);
                adjustFund(account(accounts, transaction.getToAccountId()), amount * multiplier);
            }
            case "REPAYMENT" -> {
                adjustFund(account(accounts, transaction.getFromAccountId()), -amount * multiplier);
                adjustDebt(account(accounts, transaction.getToAccountId()), -amount * multiplier);
            }
            default -> throw new BusinessException("TRANSACTION_TYPE_INVALID", "账单类型不支持");
        }
        updateAccounts(accounts, transaction, amount, multiplier);
    }

    private void updateAccounts(Map<Long, AssetAccount> accounts, TransactionDetail transaction, long amount, int multiplier) {
        List<Long> ids = new ArrayList<>(idsOf(transaction));
        ids.sort(Comparator.naturalOrder());
        for (Long id : ids) accountMapper.updateById(account(accounts, id));
    }

    private static Set<Long> requestAccountIds(String type, TransactionRequest request) {
        Set<Long> ids = new TreeSet<>();
        if ("EXPENSE".equals(type) || "INCOME".equals(type)) {
            if (request.accountId() != null) ids.add(request.accountId());
        } else {
            if (request.fromAccountId() != null) ids.add(request.fromAccountId());
            if (request.toAccountId() != null) ids.add(request.toAccountId());
        }
        return ids;
    }

    private static Set<Long> unionAccountIds(TransactionDetail old, String type, TransactionRequest request) {
        Set<Long> ids = new TreeSet<>(idsOf(old));
        ids.addAll(requestAccountIds(type, request));
        return ids;
    }

    private static Set<Long> idsOf(TransactionDetail transaction) {
        Set<Long> ids = new TreeSet<>();
        if (transaction.getAccountId() != null) ids.add(transaction.getAccountId());
        if (transaction.getFromAccountId() != null) ids.add(transaction.getFromAccountId());
        if (transaction.getToAccountId() != null) ids.add(transaction.getToAccountId());
        return ids;
    }

    private static boolean sameAccountShape(TransactionDetail old, String type, TransactionRequest request) {
        return old.getTransactionType().equals(type)
                && equals(old.getAccountId(), request.accountId())
                && equals(old.getFromAccountId(), request.fromAccountId())
                && equals(old.getToAccountId(), request.toAccountId());
    }

    private static boolean sameRequest(TransactionDetail existing, String type, long amount, TransactionRequest request) {
        return existing.getTransactionType().equals(type)
                && existing.getOriginalAmount() == amount
                && equals(existing.getAccountId(), request.accountId())
                && equals(existing.getFromAccountId(), request.fromAccountId())
                && equals(existing.getToAccountId(), request.toAccountId());
    }

    private static AssetAccount account(Map<Long, AssetAccount> accounts, Long id) {
        AssetAccount account = id == null ? null : accounts.get(id);
        if (account == null) throw new BusinessException("ACCOUNT_NOT_FOUND", "账户不存在");
        return account;
    }

    private AssetAccount ownedAccount(long id, long userId, boolean ignored) {
        AssetAccount account = accountMapper.selectOne(new LambdaQueryWrapper<AssetAccount>()
                .eq(AssetAccount::getId, id).eq(AssetAccount::getUserId, userId).eq(AssetAccount::getDeleted, 0));
        if (account == null) throw new BusinessException("ACCOUNT_NOT_FOUND", "账户不存在");
        return account;
    }

    private static void requireFund(AssetAccount account) {
        if (account == null || !"FUND".equals(account.getAccountType())) throw new BusinessException("FUND_ACCOUNT_REQUIRED", "请选择资金账户");
    }

    private static void requireCredit(AssetAccount account) {
        if (account == null || !"CREDIT".equals(account.getAccountType())) throw new BusinessException("CREDIT_ACCOUNT_REQUIRED", "请选择信贷账户");
    }

    private static void adjustFund(AssetAccount account, long delta) {
        requireFund(account);
        long next = Math.addExact(value(account.getBalanceCent()), delta);
        if (next < 0) throw new BusinessException("BALANCE_NOT_ENOUGH", "资金账户余额不足");
        account.setBalanceCent(next);
    }

    private static void adjustDebt(AssetAccount account, long delta) {
        requireCredit(account);
        long next = Math.addExact(value(account.getCurrentDebtCent()), delta);
        if (next < 0 || next > value(account.getTotalLimitCent())) throw new BusinessException("DEBT_INVALID", "信贷账户欠款超出有效范围");
        account.setCurrentDebtCent(next);
    }

    private static long validAmount(Long amount) {
        if (amount == null || amount <= 0 || amount > MAX_CENTS) throw new BusinessException("INVALID_AMOUNT", "金额必须在 ¥0.01～¥999,999,999.99 之间");
        return amount;
    }

    private static String normalizeType(String input) {
        String type = input == null ? "" : input.trim().toUpperCase(Locale.ROOT);
        if (!TYPES.contains(type)) throw new BusinessException("TRANSACTION_TYPE_INVALID", "账单类型不支持");
        return type;
    }

    private static String normalizeOptionalType(String input) {
        if (input == null || input.isBlank()) return null;
        return normalizeType(input);
    }

    private static String normalizeIdempotencyKey(String key) {
        if (key == null || key.isBlank()) return null;
        String value = key.trim();
        if (value.length() > 80) throw new BusinessException("IDEMPOTENCY_KEY_INVALID", "幂等键不能超过80个字符");
        return value;
    }

    private static String validNote(String note) {
        if (note == null || note.isBlank()) return null;
        String value = note.trim();
        if (value.codePointCount(0, value.length()) > 100) throw new BusinessException("NOTE_TOO_LONG", "备注不能超过100个字符");
        return value;
    }

    private static LocalDateTime parseOccurredAt(String value) {
        if (value == null || value.isBlank()) throw new BusinessException("DATE_INVALID", "记账时间不能为空");
        String normalized = value.trim();
        if (normalized.length() == 16) normalized += ":00";
        try {
            LocalDateTime result = LocalDateTime.parse(normalized, DATE_TIME);
            if (result.getYear() < 1000 || result.getYear() > 9999) throw new BusinessException("DATE_INVALID", "记账年份必须在1000至9999之间");
            return result;
        }
        catch (DateTimeParseException ex) { throw new BusinessException("DATE_INVALID", "记账时间格式不正确"); }
    }

    static Scope parseScope(String month, String date) {
        if (month != null && !month.isBlank() && date != null && !date.isBlank()) {
            throw new BusinessException("DATE_SCOPE_CONFLICT", "月份和日期不能同时筛选");
        }
        try {
            if (date != null && !date.isBlank()) {
                LocalDate day = LocalDate.parse(date);
                return new Scope(day.atStartOfDay(), day.plusDays(1).atStartOfDay());
            }
            if (month != null && !month.isBlank()) {
                YearMonth ym = YearMonth.parse(month);
                return new Scope(ym.atDay(1).atStartOfDay(), ym.plusMonths(1).atDay(1).atStartOfDay());
            }
            return new Scope(null, null);
        } catch (DateTimeParseException ex) {
            throw new BusinessException("DATE_INVALID", "日期或月份格式不正确");
        }
    }

    private static boolean equals(Object left, Object right) { return left == null ? right == null : left.equals(right); }
    private static long value(Long value) { return value == null ? 0 : value; }
    private static String stringId(Long value) { return value == null ? null : String.valueOf(value); }

    public static TransactionVO toVO(TransactionDetail value) {
        return new TransactionVO(String.valueOf(value.getId()), value.getTransactionNo(), value.getTransactionType(), value(value.getAmount()),
                value(value.getOriginalAmount()), Integer.valueOf(1).equals(value.getHasRefund()), stringId(value.getAccountId()),
                stringId(value.getFromAccountId()), stringId(value.getToAccountId()), value.getOccurredAt(), value.getNote(), "ACTIVE");
    }

    private static RefundVO toRefundVO(TransactionRefund value) {
        return new RefundVO(String.valueOf(value.getId()), value.getRefundNo(), value(value.getRefundAmount()), value.getCreatedAt());
    }

    public record Scope(LocalDateTime start, LocalDateTime end) {}
}
