package com.hahaen.ledger.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/** 关联表公共字段：仅 createdAt 必填且数据库默认当前时间；deleted 可空且默认 0。 */
@Data
public abstract class BaseRelationEntity {
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableLogic(value = "0", delval = "1")
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
