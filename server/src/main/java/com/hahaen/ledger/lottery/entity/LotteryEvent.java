package com.hahaen.ledger.lottery.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lottery_event")
public class LotteryEvent extends BaseAuditEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long entryId;
    private Long batchId;
    private String eventKey;
    private String kind;
    private String title;
    private String body;
    private Integer cancelled;
}
