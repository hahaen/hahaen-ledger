package com.hahaen.ledger.item.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("personal_item")
public class PersonalItem extends BaseAuditEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String name;
    private Long priceCent;
    private LocalDate purchasedOn;
    private String status;
    private LocalDate retiredOn;
    private Long resaleCent;
    private String createKey;
    private String createHash;
    private String retireKey;
    private String deleteKey;
}
