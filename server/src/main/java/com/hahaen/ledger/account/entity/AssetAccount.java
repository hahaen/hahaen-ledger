package com.hahaen.ledger.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset_account")
public class AssetAccount extends BaseAuditEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    @TableField("account_name")
    private String accountName;
    @TableField("account_type")
    private String accountType;
    @TableField("total_limit_cent")
    private Long totalLimitCent;
    @TableField("current_debt_cent")
    private Long currentDebtCent;
    @TableField("balance_cent")
    private Long balanceCent;
    @TableField("include_net_asset")
    private Integer includeNetAsset;
}
