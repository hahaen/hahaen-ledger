-- 独立物品管理，不参与记账或账户余额。
CREATE TABLE personal_item (
  id BIGINT NOT NULL COMMENT '物品ID',
  user_id BIGINT NOT NULL COMMENT '所属用户ID，不关联账本或账户',
  name VARCHAR(80) NOT NULL COMMENT '物品名称，1至40个字符',
  price_cent BIGINT NOT NULL COMMENT '购入价格，单位分，允许0元赠品',
  purchased_on DATE NOT NULL COMMENT '购买日期，含当日计为服役第1天',
  status VARCHAR(16) NOT NULL COMMENT '服役状态，ACTIVE正在服役，RETIRED已退役',
  retired_on DATE NULL COMMENT '退役日期，退役后服役天数停止增长',
  resale_cent BIGINT NULL COMMENT '二手出售价格，单位分，0表示未出售，允许高于购入价',
  create_key VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '创建请求幂等键，用户内唯一且删除后保留',
  create_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '原始创建请求SHA256摘要，用于生命周期变化后的重试校验',
  retire_key VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '退役请求幂等键',
  delete_key VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '删除请求幂等键',
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
  UNIQUE KEY uk_personal_item_user_create_key (user_id, create_key),
  KEY idx_personal_item_user_deleted_date (user_id, deleted, purchased_on, id),
  CONSTRAINT fk_personal_item_user FOREIGN KEY (user_id) REFERENCES app_user (id),
  CONSTRAINT ck_personal_item_name CHECK (CHAR_LENGTH(TRIM(name)) BETWEEN 1 AND 40),
  CONSTRAINT ck_personal_item_price CHECK (price_cent BETWEEN 0 AND 99999999999),
  CONSTRAINT ck_personal_item_purchase_date CHECK (purchased_on >= '1000-01-01'),
  CONSTRAINT ck_personal_item_lifecycle CHECK (
    (status = 'ACTIVE' AND retired_on IS NULL AND resale_cent IS NULL)
    OR (status = 'RETIRED' AND retired_on IS NOT NULL AND retired_on >= purchased_on
        AND resale_cent IS NOT NULL AND resale_cent BETWEEN 0 AND 99999999999)
  ),
  CONSTRAINT ck_personal_item_deleted CHECK (deleted IS NULL OR deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='独立个人物品表，管理服役与成本，不参与记账';
