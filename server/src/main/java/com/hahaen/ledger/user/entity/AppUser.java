package com.hahaen.ledger.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_user")
public class AppUser extends BaseAuditEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String loginAccount;
    private String passwordHash;
    private String nickname;
    private Long avatarFileId;
    private String status;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
}
