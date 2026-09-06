package com.hahaen.ledger.account.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true) @TableName("ledger_account")
public class LedgerAccount extends BaseAuditEntity { @TableId private Long id; private Long bookId; private String name; private String kind; private Long balanceCents; private Long creditLimitCents; private Boolean includedInNetAsset; private String status; }
