package com.hahaen.ledger.lottery.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lottery_draw")
public class LotteryDraw extends BaseAuditEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String game;
    private String issue;
    private String nextIssue;
    private LocalDate drawDate;
    private String red;
    private String blue;
    private String prizes;
    private Long poolCents;
    private String specialState;
    private Integer promotion;
    private String source;
    private String ruleVersion;
    private String sourceHash;
    private LocalDateTime syncedAt;
    private Integer version;
}
