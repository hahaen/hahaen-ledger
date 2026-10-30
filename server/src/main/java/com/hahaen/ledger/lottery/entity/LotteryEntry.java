package com.hahaen.ledger.lottery.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lottery_entry")
public class LotteryEntry extends BaseAuditEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long planId;
    private Long batchId;
    private Integer position;
    private String game;
    private String issue;
    private String red;
    private String blue;
    private Integer multiple;
    private Integer additional;
    private LocalDate purchasedOn;
    private String note;
    private Long costCents;
    private Long prizeCents;
    private String result;
    private String grade;
    private Long drawId;
    private Integer drawVersion;
    private Integer revision;
    private LocalDateTime acceptedAt;
    private String resultHash;
}
