-- 哈记账当前开发阶段的数据库初始基线。
-- 本迁移仅建立用户主表及微信身份关联表，业务实体和接口暂不在本次实现。

CREATE TABLE app_user (
  id BIGINT NOT NULL COMMENT '用户ID',
  login_account VARCHAR(64) NULL COMMENT 'H5登录账号，账号密码认证使用，微信用户可为空',
  password_hash VARCHAR(255) NULL COMMENT '服务端密码哈希，仅保存哈希值，不保存明文密码或前端加密原文',
  nickname VARCHAR(40) NOT NULL DEFAULT '账本主人' COMMENT '用户昵称',
  avatar_file_id BIGINT NULL COMMENT '头像文件ID，未授权或未上传头像时为空',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '用户状态，ACTIVE正常，DISABLED停用',
  last_login_at DATETIME(3) NULL COMMENT '最近登录时间',
  last_login_ip VARCHAR(45) NULL COMMENT '最近登录IP地址，支持IPv4和IPv6',
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
  UNIQUE KEY uk_app_user_login_account (login_account),
  KEY idx_app_user_status_deleted (status, deleted),
  CONSTRAINT ck_app_user_status CHECK (status IN ('ACTIVE', 'DISABLED')),
  CONSTRAINT ck_app_user_h5_credentials CHECK (
    (login_account IS NULL AND password_hash IS NULL)
    OR
    (login_account IS NOT NULL AND password_hash IS NOT NULL)
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用用户表';

CREATE TABLE user_identity (
  id BIGINT NOT NULL COMMENT '用户身份记录ID',
  user_id BIGINT NOT NULL COMMENT '关联用户ID',
  provider VARCHAR(32) NOT NULL COMMENT '身份提供方，例如 WECHAT_MINI_PROGRAM',
  open_id VARCHAR(128) NOT NULL COMMENT '身份提供方用户标识，不返回给前端',
  union_id VARCHAR(128) NULL COMMENT '微信开放平台统一标识',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  deleted TINYINT NULL DEFAULT 0 COMMENT '删除标识，0存在1删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_identity_provider_open_id (provider, open_id),
  KEY idx_identity_user (user_id),
  CONSTRAINT fk_identity_user FOREIGN KEY (user_id) REFERENCES app_user (id),
  CONSTRAINT ck_user_identity_open_id CHECK (CHAR_LENGTH(TRIM(open_id)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户第三方身份关联表';

CREATE TABLE app_login_log (
  id BIGINT NOT NULL COMMENT '登录日志ID',
  user_id BIGINT NULL COMMENT '关联用户ID，登录失败且无法识别用户时为空',
  login_channel VARCHAR(32) NOT NULL COMMENT '登录渠道，H5_PASSWORD账号密码，WECHAT_MINI_PROGRAM微信小程序',
  login_result VARCHAR(16) NOT NULL COMMENT '登录结果，SUCCESS成功，FAILURE失败',
  login_account VARCHAR(64) NULL COMMENT '登录账号快照，微信登录时为空',
  login_ip VARCHAR(45) NULL COMMENT '登录IP地址，支持IPv4和IPv6',
  user_agent VARCHAR(512) NULL COMMENT '登录客户端User-Agent或设备信息',
  failure_code VARCHAR(64) NULL COMMENT '登录失败原因编码，成功时为空',
  trace_id VARCHAR(64) NULL COMMENT '请求Trace ID，用于关联服务端日志',
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
  KEY idx_login_log_user_created (user_id, created_at),
  KEY idx_login_log_account_created (login_account, created_at),
  KEY idx_login_log_result_created (login_result, created_at),
  CONSTRAINT fk_login_log_user FOREIGN KEY (user_id) REFERENCES app_user (id),
  CONSTRAINT ck_login_log_channel CHECK (login_channel IN ('H5_PASSWORD', 'WECHAT_MINI_PROGRAM')),
  CONSTRAINT ck_login_log_result CHECK (login_result IN ('SUCCESS', 'FAILURE')),
  CONSTRAINT ck_login_log_failure_code CHECK (
    (login_result = 'SUCCESS' AND failure_code IS NULL)
    OR
    (login_result = 'FAILURE' AND failure_code IS NOT NULL)
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用登录日志表';
