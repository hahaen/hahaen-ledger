package com.hahaen.ledger.account.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hahaen.ledger.account.dto.AccountRequest;
import com.hahaen.ledger.account.entity.LedgerAccount;
import com.hahaen.ledger.account.mapper.LedgerAccountMapper;
import com.hahaen.ledger.book.service.BookService;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.AuditSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service @RequiredArgsConstructor
public class AccountService {
    private final LedgerAccountMapper accountMapper; private final BookService bookService;
    public List<LedgerAccount> list() { return accountMapper.selectList(new LambdaQueryWrapper<LedgerAccount>().eq(LedgerAccount::getBookId, bookService.current().getId()).in(LedgerAccount::getStatus, "ACTIVE", "DISABLED").orderByAsc(LedgerAccount::getCreatedAt)); }
    public LedgerAccount require(long id) { LedgerAccount a = accountMapper.selectOne(new LambdaQueryWrapper<LedgerAccount>().eq(LedgerAccount::getId, id).eq(LedgerAccount::getBookId, bookService.current().getId())); if (a == null) throw new BusinessException("ACCOUNT_NOT_FOUND", "账户不存在"); return a; }
    @Transactional public LedgerAccount create(AccountRequest request) {
        if (!request.kind().equals("FUND") && !request.kind().equals("CREDIT")) throw new BusinessException("INVALID_ACCOUNT_KIND", "账户类型不合法");
        if (request.kind().equals("CREDIT") && request.balanceCents() > request.creditLimitCents()) throw new BusinessException("CREDIT_LIMIT_EXCEEDED", "欠款不能超过总额度");
        long bookId = bookService.current().getId(); if (accountMapper.selectCount(new LambdaQueryWrapper<LedgerAccount>().eq(LedgerAccount::getBookId, bookId).eq(LedgerAccount::getName, request.name())) > 0) throw new BusinessException("ACCOUNT_NAME_EXISTS", "账户名称已存在");
        LedgerAccount a = new LedgerAccount(); a.setBookId(bookId); a.setName(request.name()); a.setKind(request.kind()); a.setBalanceCents(request.balanceCents()); a.setCreditLimitCents(request.kind().equals("CREDIT") ? request.creditLimitCents() : 0L); a.setIncludedInNetAsset(request.includedInNetAsset()); a.setStatus("ACTIVE"); a.setCreatedAt(LocalDateTime.now()); a.setUpdatedAt(LocalDateTime.now()); accountMapper.insert(a); return a;
    }
    @Transactional public LedgerAccount update(long id, AccountRequest request) { LedgerAccount a = require(id); if (!a.getKind().equals(request.kind())) throw new BusinessException("ACCOUNT_KIND_IMMUTABLE", "账户类型不能修改"); if (accountMapper.selectCount(new LambdaQueryWrapper<LedgerAccount>().eq(LedgerAccount::getBookId,a.getBookId()).eq(LedgerAccount::getName,request.name()).ne(LedgerAccount::getId,id)) > 0) throw new BusinessException("ACCOUNT_NAME_EXISTS", "账户名称已存在"); if (a.getKind().equals("CREDIT") && request.balanceCents() > request.creditLimitCents()) throw new BusinessException("CREDIT_LIMIT_EXCEEDED", "欠款不能超过总额度"); a.setName(request.name()); a.setBalanceCents(request.balanceCents()); a.setCreditLimitCents(a.getKind().equals("CREDIT") ? request.creditLimitCents() : 0L); a.setIncludedInNetAsset(request.includedInNetAsset()); a.setUpdatedAt(LocalDateTime.now()); accountMapper.updateById(a); return a; }
    @Transactional public void disable(long id) { LedgerAccount a = require(id); a.setStatus("DISABLED"); AuditSupport.markDeleted(a); accountMapper.updateById(a); }
}
