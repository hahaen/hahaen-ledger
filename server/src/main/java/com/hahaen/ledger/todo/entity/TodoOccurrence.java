package com.hahaen.ledger.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data @EqualsAndHashCode(callSuper = true) @TableName("ha_todo_occurrence")
public class TodoOccurrence extends BaseAuditEntity {
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    private Long ruleId;
    private Long userId;
    private LocalDateTime dueAt;
    private LocalDateTime anchorAt;
    private String title;
    private String note;
    private String recurrence;
    private Integer monthInterval;
    private Integer remind;
    private String status;
    private LocalDateTime completedAt;
}
