package com.hahaen.ledger.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_login_log")
public class AppLoginLog extends BaseAuditEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String loginChannel;
    private String loginResult;
    private String loginAccount;
    private String loginIp;
    private String userAgent;
    private String failureCode;
    private String traceId;
}
