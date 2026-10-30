package com.hahaen.ledger.lottery.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lottery_sync_state")
public class LotterySyncState extends BaseAuditEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String game;
    private Integer historyPage;
    private Integer catchupPage;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String catchupIssue;
    private Integer historyDone;
    private LocalDateTime lastSuccessAt;
    private String lastError;
    private LocalDateTime lockedUntil;
    private String leaseToken;
}
