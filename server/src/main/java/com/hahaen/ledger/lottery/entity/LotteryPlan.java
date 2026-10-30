package com.hahaen.ledger.lottery.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lottery_plan")
public class LotteryPlan extends BaseAuditEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String game;
    private String name;
    private String red;
    private String blue;
    private Integer multiple;
    private Integer additional;
    private String status;
    private String note;
}
