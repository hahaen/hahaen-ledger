package com.hahaen.ledger.transaction.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data @EqualsAndHashCode(callSuper = true) @TableName("transaction_refund")
public class TransactionRefund extends BaseAuditEntity { @TableId private Long id; private Long transactionId; private Long amountCents; private LocalDateTime refundedAt; private String status; private String idempotencyKey; }
