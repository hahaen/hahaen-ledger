package com.hahaen.ledger.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 主表统一审计字段，仅 createdAt 必填，数据库默认当前时间。
 * 其他公共字段允许为空，deleted 数据库默认 0，应用插入时同样填充 0。
 * 名称字段只用于展示和历史追踪，不参与权限判断。
 */
@Data
public abstract class BaseAuditEntity {
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private String createdName;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateName;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime deletedAt;
    @TableField(fill = FieldFill.UPDATE)
    private Long deletedBy;
    @TableField(fill = FieldFill.UPDATE)
    private String deletedName;
    @TableLogic(value = "0", delval = "1")
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
