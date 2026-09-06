package com.hahaen.ledger.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hahaen.ledger.common.entity.BaseRelationEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true) @TableName("user_identity")
public class UserIdentity extends BaseRelationEntity { @TableId private Long id; private Long userId; private String provider; private String openId; private String unionId; }
