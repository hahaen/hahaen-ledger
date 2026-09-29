CREATE TABLE user_notification_config (
  id BIGINT NOT NULL COMMENT '通知配置ID',
  user_id BIGINT NOT NULL COMMENT '所属用户ID',
  notification_type VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '通知类型，例如 BARK、PUSHPLUS，可扩展',
  notification_key VARCHAR(1024) NOT NULL COMMENT '通知Key的AES-256-GCM密文，不保存明文',
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
  UNIQUE KEY uk_notification_config_user_type (user_id, notification_type),
  CONSTRAINT fk_notification_config_user FOREIGN KEY (user_id) REFERENCES app_user (id),
  CONSTRAINT ck_notification_config_deleted CHECK (deleted IS NULL OR deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户通用通知配置表';

CREATE TABLE user_notification_config_request (
  user_id BIGINT NOT NULL COMMENT '所属用户ID',
  idempotency_key VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '通知配置写入请求幂等键，用户内唯一',
  request_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '请求类型和密文的SHA256摘要，用于重复请求核对',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  deleted TINYINT NULL DEFAULT 0 COMMENT '删除标识，0存在1删除',
  PRIMARY KEY (user_id, idempotency_key),
  CONSTRAINT fk_notification_request_user FOREIGN KEY (user_id) REFERENCES app_user (id),
  CONSTRAINT ck_notification_request_deleted CHECK (deleted IS NULL OR deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户通知配置幂等请求关系表';
