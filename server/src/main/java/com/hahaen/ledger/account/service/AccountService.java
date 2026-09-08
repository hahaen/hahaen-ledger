package com.hahaen.ledger.account.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hahaen.ledger.account.dto.AccountRequest;
import com.hahaen.ledger.account.dto.AccountOrderRequest;
import com.hahaen.ledger.account.entity.AssetAccount;
import com.hahaen.ledger.account.mapper.AssetAccountMapper;
import com.hahaen.ledger.account.vo.AccountVO;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.AuditSupport;
import com.hahaen.ledger.common.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AccountService {
    public static final long MAX_CENTS = 99_999_999_999L;

    private final AssetAccountMapper accountMapper;

    public List<AccountVO> list() {
        return accountMapper.selectActiveByUser(CurrentUser.id()).stream().map(AccountService::toVO).toList();
    }

    public AccountVO get(long id) {
        return toVO(owned(id, false));
    }

    @Transactional
    public AccountVO create(AccountRequest request) {
        long userId = CurrentUser.id();
        String name = validName(request.name());
        String type = validType(request.kind());
        assertNameAvailable(userId, name, null);
        AssetAccount account = new AssetAccount();
        account.setUserId(userId);
        account.setAccountName(name);
        account.setAccountType(type);
        account.setSortOrder(nextSortOrder(userId, type));
        account.setIncludeNetAsset(Boolean.FALSE.equals(request.includedInNetAsset()) ? 0 : 1);
        applyAmounts(account, type, request.balanceCents(), request.creditLimitCents(), request.currentDebtCents());
        accountMapper.insert(account);
        return toVO(account);
    }

    @Transactional
    public AccountVO update(long id, AccountRequest request) {
        long userId = CurrentUser.id();
        AssetAccount account = ownedForUpdate(id, userId);
        String name = validName(request.name());
        String type = validType(request.kind());
        assertNameAvailable(userId, name, id);
        if (!type.equals(account.getAccountType())) {
            throw new BusinessException("ACCOUNT_TYPE_IMMUTABLE", "账户类型创建后不能修改");
        }
        account.setAccountName(name);
        account.setIncludeNetAsset(Boolean.FALSE.equals(request.includedInNetAsset()) ? 0 : 1);
        applyAmounts(account, type, request.balanceCents(), request.creditLimitCents(), request.currentDebtCents());
        accountMapper.updateById(account);
        return toVO(account);
    }

    @Transactional
    public List<AccountVO> reorder(long id, AccountOrderRequest request) {
        long userId = CurrentUser.id();
        if (id == request.targetAccountId()) return list();

        AssetAccount first = ownedForUpdate(Math.min(id, request.targetAccountId()), userId);
        AssetAccount second = ownedForUpdate(Math.max(id, request.targetAccountId()), userId);
        AssetAccount source = id == first.getId() ? first : second;
        AssetAccount target = request.targetAccountId() == first.getId() ? first : second;
        if (!source.getAccountType().equals(target.getAccountType())) {
            throw new BusinessException("ACCOUNT_ORDER_TYPE_MISMATCH", "只能调整同类账户顺序");
        }

        int sourceOrder = value(source.getSortOrder());
        int targetOrder = value(target.getSortOrder());
        boolean pending = sourceOrder == request.expectedSortOrder()
                && targetOrder == request.targetExpectedSortOrder();
        boolean alreadyApplied = sourceOrder == request.targetExpectedSortOrder()
                && targetOrder == request.expectedSortOrder();
        if (!pending && !alreadyApplied) {
            throw new BusinessException("ACCOUNT_ORDER_CONFLICT", "账户顺序已变化，请重新选择");
        }
        if (pending) {
            source.setSortOrder(targetOrder);
            target.setSortOrder(sourceOrder);
            accountMapper.updateById(source);
            accountMapper.updateById(target);
        }
        return list();
    }

    @Transactional
    public void delete(long id) {
        AssetAccount account = ownedForUpdate(id, CurrentUser.id());
        AuditSupport.markDeleted(account);
        accountMapper.updateById(account);
    }

    public AssetAccount owned(long id, boolean forUpdate) {
        long userId = CurrentUser.id();
        return forUpdate ? ownedForUpdate(id, userId) : owned(id, userId);
    }

    public AssetAccount owned(long id, long userId) {
        AssetAccount account = accountMapper.selectOne(new LambdaQueryWrapper<AssetAccount>()
                .eq(AssetAccount::getId, id)
                .eq(AssetAccount::getUserId, userId)
                .eq(AssetAccount::getDeleted, 0));
        if (account == null) throw new BusinessException("ACCOUNT_NOT_FOUND", "账户不存在");
        return account;
    }

    public AssetAccount ownedForUpdate(long id, long userId) {
        AssetAccount account = accountMapper.selectOwnedForUpdate(id, userId);
        if (account == null) throw new BusinessException("ACCOUNT_NOT_FOUND", "账户不存在");
        return account;
    }

    private void assertNameAvailable(long userId, String name, Long excludingId) {
        LambdaQueryWrapper<AssetAccount> query = new LambdaQueryWrapper<AssetAccount>()
                .eq(AssetAccount::getUserId, userId)
                .eq(AssetAccount::getAccountName, name)
                .eq(AssetAccount::getDeleted, 0);
        if (excludingId != null) query.ne(AssetAccount::getId, excludingId);
        if (accountMapper.selectCount(query) > 0) {
            throw new BusinessException("ACCOUNT_NAME_EXISTS", "账户名称不能重复");
        }
    }

    private static void applyAmounts(AssetAccount account, String type, Long balance, Long limit, Long debt) {
        if ("FUND".equals(type)) {
            long value = nonNegative(balance, "余额");
            account.setBalanceCent(value);
            account.setTotalLimitCent(null);
            account.setCurrentDebtCent(null);
            return;
        }
        if (limit == null || debt == null) throw new BusinessException("ACCOUNT_AMOUNT_REQUIRED", "信贷账户需要填写总额度和当前欠款");
        long limitValue = nonNegative(limit, "总额度");
        long debtValue = nonNegative(debt, "当前欠款");
        if (debtValue > limitValue) throw new BusinessException("ACCOUNT_DEBT_EXCEEDS_LIMIT", "当前欠款不能超过总额度");
        account.setBalanceCent(null);
        account.setTotalLimitCent(limitValue);
        account.setCurrentDebtCent(debtValue);
    }

    private static long nonNegative(Long value, String label) {
        if (value == null || value < 0 || value > MAX_CENTS) {
            throw new BusinessException("INVALID_AMOUNT", label + "必须在 ¥0～¥999,999,999.99 之间");
        }
        return value;
    }

    private int nextSortOrder(long userId, String type) {
        Integer max = accountMapper.selectMaxSortOrderForUpdate(userId, type);
        return (max == null ? 0 : max) + 1;
    }

    private static String validName(String input) {
        String name = input == null ? "" : input.trim();
        if (name.isEmpty() || name.codePointCount(0, name.length()) > 20) {
            throw new BusinessException("ACCOUNT_NAME_INVALID", "账户名称需为1至20个字符");
        }
        return name;
    }

    private static String validType(String input) {
        String type = input == null ? "" : input.trim().toUpperCase(Locale.ROOT);
        if (!"FUND".equals(type) && !"CREDIT".equals(type)) {
            throw new BusinessException("ACCOUNT_TYPE_INVALID", "账户类型不支持");
        }
        return type;
    }

    public static AccountVO toVO(AssetAccount account) {
        boolean fund = "FUND".equals(account.getAccountType());
        long balance = fund ? value(account.getBalanceCent()) : value(account.getCurrentDebtCent());
        long limit = fund ? 0 : value(account.getTotalLimitCent());
        return new AccountVO(String.valueOf(account.getId()), account.getAccountName(), account.getAccountType(), value(account.getSortOrder()), balance, limit,
                Integer.valueOf(1).equals(account.getIncludeNetAsset()), "ACTIVE");
    }

    private static long value(Long value) {
        return value == null ? 0 : value;
    }

    private static int value(Integer value) {
        return value == null ? 0 : value;
    }
}
