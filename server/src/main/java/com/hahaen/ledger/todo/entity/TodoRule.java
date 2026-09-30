package com.hahaen.ledger.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data @EqualsAndHashCode(callSuper = true) @TableName("ha_todo_rule")
public class TodoRule extends BaseAuditEntity {
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    private Long userId;
    private String title;
    private String note;
    private String recurrence;
    private Integer monthInterval;
    private String repeatMode;
    private String repeatUnit;
    private Integer repeatInterval;
    private String weekDays;
    private String monthDays;
    private Integer lastDay;
    private String yearDays;
    private String fixedDates;

    private LocalDateTime anchorAt;
    private LocalDateTime nextDueAt;
    private Integer remind;
    private Integer active;
}
