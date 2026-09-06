-- 文件元数据表。
-- 不保存永久签名 URL，MinIO 预览地址由后端鉴权后按需生成。

CREATE TABLE app_file (
  id BIGINT NOT NULL COMMENT '文件ID',
  user_id BIGINT NOT NULL COMMENT '文件所属用户ID',
  book_id BIGINT NULL COMMENT '文件所属账本ID，用户头像文件为空',
  transaction_id BIGINT NULL COMMENT '文件所属账单ID，非账单附件时为空',
  business_type VARCHAR(32) NOT NULL COMMENT '文件业务类型，AVATAR用户头像，TRANSACTION_ATTACHMENT账单附件',
  storage_provider VARCHAR(16) NOT NULL DEFAULT 'MINIO' COMMENT '存储提供方，当前为MINIO',
  bucket_name VARCHAR(128) NOT NULL COMMENT 'MinIO Bucket名称，不向前端暴露',
  object_key VARCHAR(512) NOT NULL COMMENT 'MinIO对象Key，不是永久访问URL',
  original_name VARCHAR(255) NULL COMMENT '用户上传时的原始文件名',
  content_type VARCHAR(128) NOT NULL COMMENT '文件MIME类型',
  file_size BIGINT NOT NULL COMMENT '文件大小，单位为字节',
  file_hash CHAR(64) NULL COMMENT '文件SHA-256摘要，用于完整性校验或去重',
  storage_etag VARCHAR(128) NULL COMMENT 'MinIO对象ETag',
  status VARCHAR(16) NOT NULL DEFAULT 'UPLOADING' COMMENT '文件状态，UPLOADING上传中，READY可用，FAILED失败，DELETING删除中，DELETED已删除',
  idempotency_key VARCHAR(80) NULL COMMENT '上传或确认操作幂等键',
  uploaded_at DATETIME(3) NULL COMMENT '文件确认上传完成时间',
  failure_code VARCHAR(64) NULL COMMENT '文件处理失败原因编码，成功时为空',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  created_by BIGINT NULL COMMENT '创建人ID',
  created_name VARCHAR(100) NULL COMMENT '创建人',
  updated_at DATETIME(3) NULL COMMENT '更新时间',
  updated_by BIGINT NULL COMMENT '更新人ID',
  update_name VARCHAR(100) NULL COMMENT '更新人',
  deleted_at DATETIME(3) NULL COMMENT '删除时间',
  deleted_by BIGINT NULL COMMENT '删除人ID',
  deleted_name VARCHAR(100) NULL COMMENT '删除人',
  deleted TINYINT NULL DEFAULT 0 COMMENT '删除标识，0存在1删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_app_file_storage_object (storage_provider, bucket_name, object_key),
  UNIQUE KEY uk_app_file_user_idempotency (user_id, idempotency_key),
  KEY idx_app_file_user_status (user_id, status, deleted),
  KEY idx_app_file_book_status (book_id, status, deleted),
  KEY idx_app_file_transaction_status (transaction_id, status, deleted),
  KEY idx_app_file_business_type (business_type, created_at),
  CONSTRAINT fk_app_file_user FOREIGN KEY (user_id) REFERENCES app_user (id),
  CONSTRAINT ck_app_file_business_type CHECK (business_type IN ('AVATAR', 'TRANSACTION_ATTACHMENT')),
  CONSTRAINT ck_app_file_storage_provider CHECK (storage_provider = 'MINIO'),
  CONSTRAINT ck_app_file_status CHECK (status IN ('UPLOADING', 'READY', 'FAILED', 'DELETING', 'DELETED')),
  CONSTRAINT ck_app_file_size CHECK (file_size >= 0),
  CONSTRAINT ck_app_file_sha256 CHECK (file_hash IS NULL OR CHAR_LENGTH(file_hash) = 64),
  CONSTRAINT ck_app_file_business_relation CHECK (
    (business_type = 'AVATAR' AND book_id IS NULL AND transaction_id IS NULL)
    OR
    (business_type = 'TRANSACTION_ATTACHMENT' AND book_id IS NOT NULL AND transaction_id IS NOT NULL)
  ),
  CONSTRAINT ck_app_file_status_fields CHECK (
    (status = 'READY' AND uploaded_at IS NOT NULL AND failure_code IS NULL)
    OR
    (status = 'FAILED' AND failure_code IS NOT NULL)
    OR
    (status IN ('UPLOADING', 'DELETING', 'DELETED') AND failure_code IS NULL)
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用文件元数据表';

ALTER TABLE app_user
  ADD CONSTRAINT fk_app_user_avatar_file
  FOREIGN KEY (avatar_file_id) REFERENCES app_file (id);
