package com.hahaen.ledger.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_file")
public class AppFile extends BaseAuditEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long bookId;
    private Long transactionId;
    private String businessType;
    private String storageProvider;
    private String bucketName;
    private String objectKey;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private String fileHash;
    private String storageEtag;
    private String status;
    private String idempotencyKey;
    private LocalDateTime uploadedAt;
    private String failureCode;
}
