package com.hahaen.ledger.book.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hahaen.ledger.book.entity.LedgerBook;
import com.hahaen.ledger.book.mapper.LedgerBookMapper;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class BookService {
    private final LedgerBookMapper bookMapper;
    public LedgerBook current() {
        LedgerBook book = bookMapper.selectOne(new LambdaQueryWrapper<LedgerBook>().eq(LedgerBook::getUserId, CurrentUser.id()).eq(LedgerBook::getStatus, "ACTIVE").last("limit 1"));
        if (book == null) throw new BusinessException("BOOK_NOT_FOUND", "当前账本不存在");
        return book;
    }
}
