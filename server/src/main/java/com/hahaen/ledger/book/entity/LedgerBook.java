package com.hahaen.ledger.book.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true) @TableName("ledger_book")
public class LedgerBook extends BaseAuditEntity { @TableId private Long id; private Long userId; private String name; private String currency; private String timezone; private String status; }
