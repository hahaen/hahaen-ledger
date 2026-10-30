package com.hahaen.ledger.lottery.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lottery_draw_revision")
public class LotteryDrawRevision extends BaseAuditEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long drawId;
    private String sourceHash;
    private String rawJson;
}
