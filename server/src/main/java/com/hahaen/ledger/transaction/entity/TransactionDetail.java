package com.hahaen.ledger.transaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("transaction_detail")
public class TransactionDetail extends BaseAuditEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String transactionNo;
    private String transactionType;
    private Long originalAmount;
    private Long amount;
    private Integer hasRefund;
    private Long accountId;
    private Long fromAccountId;
    private Long toAccountId;
    private LocalDateTime occurredAt;
    private String note;
    private String idempotencyKey;
}
