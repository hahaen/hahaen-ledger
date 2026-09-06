package com.hahaen.ledger.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true) @TableName("app_user")
public class AppUser extends BaseAuditEntity { @TableId private Long id; private String nickname; private Long avatarFileId; }
