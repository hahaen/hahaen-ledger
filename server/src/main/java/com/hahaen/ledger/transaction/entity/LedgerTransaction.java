package com.hahaen.ledger.transaction.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data @EqualsAndHashCode(callSuper = true) @TableName("ledger_transaction")
public class LedgerTransaction extends BaseAuditEntity { @TableId private Long id; private Long bookId; private String type; private Long amountCents; private Long accountId; private Long fromAccountId; private Long toAccountId; private LocalDateTime occurredAt; private String note; private String status; private String idempotencyKey; }
