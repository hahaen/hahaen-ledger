-- 资产账户主表。
-- 资金账户和信贷账户共用一张表，通过 account_type 区分账户语义。
-- 当前产品为单用户、单账本模型，账户仅通过 user_id 归属用户。

CREATE TABLE asset_account (
  id BIGINT NOT NULL COMMENT '账户ID',
  user_id BIGINT NOT NULL COMMENT '所属用户ID',
  account_name VARCHAR(64) NOT NULL COMMENT '账户名称，长度限制为1至20个字符，允许重复',
  account_type VARCHAR(16) NOT NULL COMMENT '账户类型，FUND资金账户，CREDIT信贷账户',
  total_limit_cent BIGINT NULL COMMENT '总额度，单位为分；仅CREDIT信贷账户使用',
  current_debt_cent BIGINT NULL COMMENT '当前欠款，单位为分；仅CREDIT信贷账户使用',
  balance_cent BIGINT NULL COMMENT '余额，单位为分；仅FUND资金账户使用',
  include_net_asset TINYINT NOT NULL DEFAULT 1 COMMENT '是否计入净资产，0不计入，1计入',
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
  KEY idx_asset_account_user_type_deleted (user_id, account_type, deleted),
  CONSTRAINT fk_asset_account_user FOREIGN KEY (user_id) REFERENCES app_user (id),
  CONSTRAINT ck_asset_account_name CHECK (
    CHAR_LENGTH(TRIM(account_name)) BETWEEN 1 AND 20
  ),
  CONSTRAINT ck_asset_account_type CHECK (
    account_type IN ('FUND', 'CREDIT')
  ),
  CONSTRAINT ck_asset_account_include_net_asset CHECK (
    include_net_asset IN (0, 1)
  ),
  CONSTRAINT ck_asset_account_deleted CHECK (
    deleted IS NULL OR deleted IN (0, 1)
  ),
  CONSTRAINT ck_asset_account_amounts_non_negative CHECK (
    (total_limit_cent IS NULL OR total_limit_cent >= 0)
    AND (current_debt_cent IS NULL OR current_debt_cent >= 0)
    AND (balance_cent IS NULL OR balance_cent >= 0)
  ),
  CONSTRAINT ck_asset_account_type_amounts CHECK (
    (
      account_type = 'FUND'
      AND balance_cent IS NOT NULL
      AND total_limit_cent IS NULL
      AND current_debt_cent IS NULL
    )
    OR
    (
      account_type = 'CREDIT'
      AND balance_cent IS NULL
      AND total_limit_cent IS NOT NULL
      AND current_debt_cent IS NOT NULL
      AND current_debt_cent <= total_limit_cent
    )
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产账户表，统一保存资金账户和信贷账户';
