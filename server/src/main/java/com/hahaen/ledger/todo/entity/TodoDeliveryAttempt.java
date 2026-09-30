package com.hahaen.ledger.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data @EqualsAndHashCode(callSuper = true) @TableName("ha_todo_delivery_attempt")
public class TodoDeliveryAttempt extends BaseAuditEntity {
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    private Long deliveryId;
    private Long occurrenceId;
    private String channel;
    private String messageTitle;
    private String messageBody;
    private String result;
    private LocalDateTime attemptedAt;
}
