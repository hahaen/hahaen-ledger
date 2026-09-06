-- 账单明细与退款记录表。
-- 当前项目为单用户、单账本模型，账单通过 user_id 归属当前用户及其唯一账本。
-- 金额统一使用整数分，避免浮点精度误差。

CREATE TABLE transaction_detail (
  id BIGINT NOT NULL COMMENT '账单明细ID',
  user_id BIGINT NOT NULL COMMENT '所属用户ID，权限校验使用当前会话用户',
  transaction_no VARCHAR(64) NOT NULL COMMENT '账单记录编号，面向详情和人工核对，必须唯一',
  transaction_type VARCHAR(16) NOT NULL COMMENT '账单类型，EXPENSE支出，INCOME收入，TRANSFER转账，REPAYMENT还款；退款不属于此字段取值',
  original_amount BIGINT NOT NULL COMMENT '原始金额，单位为分；创建后作为退款上限基准，不因退款覆盖',
  amount BIGINT NOT NULL COMMENT '当前有效金额，单位为分；原始金额减去有效退款累计金额',
  has_refund TINYINT NOT NULL DEFAULT 0 COMMENT '是否发生过退款，0未发生，1已发生；仅支出和收入可置为1',
  account_id BIGINT NULL COMMENT '单账户账单使用的资金账户ID，仅EXPENSE支出或INCOME收入适用',
  from_account_id BIGINT NULL COMMENT '转出/还款来源账户ID，TRANSFER转账或REPAYMENT还款适用，必须为资金账户',
  to_account_id BIGINT NULL COMMENT '转入/还款目标账户ID，TRANSFER转账或REPAYMENT还款适用；还款时必须为信贷账户',
  occurred_at DATETIME(3) NOT NULL COMMENT '业务记账日期与时间，使用用户设备本地时间换算后的服务端值',
  note VARCHAR(100) NULL COMMENT '账单备注，最多100个字符',
  idempotency_key VARCHAR(80) NULL COMMENT '创建或编辑账单的幂等键，由服务端按用户范围校验',
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
  UNIQUE KEY uk_transaction_detail_transaction_no (transaction_no),
  UNIQUE KEY uk_transaction_detail_user_idempotency (user_id, idempotency_key),
  KEY idx_transaction_detail_user_occurred (user_id, occurred_at, deleted),
  KEY idx_transaction_detail_user_type_occurred (user_id, transaction_type, occurred_at, deleted),
  KEY idx_transaction_detail_account_occurred (account_id, occurred_at, deleted),
  KEY idx_transaction_detail_from_account_occurred (from_account_id, occurred_at, deleted),
  KEY idx_transaction_detail_to_account_occurred (to_account_id, occurred_at, deleted),
  CONSTRAINT fk_transaction_detail_user FOREIGN KEY (user_id) REFERENCES app_user (id),
  CONSTRAINT fk_transaction_detail_account FOREIGN KEY (account_id) REFERENCES asset_account (id),
  CONSTRAINT fk_transaction_detail_from_account FOREIGN KEY (from_account_id) REFERENCES asset_account (id),
  CONSTRAINT fk_transaction_detail_to_account FOREIGN KEY (to_account_id) REFERENCES asset_account (id),
  CONSTRAINT ck_transaction_detail_transaction_no CHECK (CHAR_LENGTH(TRIM(transaction_no)) > 0),
  CONSTRAINT ck_transaction_detail_type CHECK (
    transaction_type IN ('EXPENSE', 'INCOME', 'TRANSFER', 'REPAYMENT')
  ),
  CONSTRAINT ck_transaction_detail_amounts CHECK (
    original_amount > 0
    AND original_amount <= 99999999999
    AND amount >= 0
    AND amount <= original_amount
  ),
  CONSTRAINT ck_transaction_detail_has_refund CHECK (
    has_refund IN (0, 1)
    AND (has_refund = 1 OR amount = original_amount)
  ),
  CONSTRAINT ck_transaction_detail_type_accounts CHECK (
    (
      transaction_type IN ('EXPENSE', 'INCOME')
      AND account_id IS NOT NULL
      AND from_account_id IS NULL
      AND to_account_id IS NULL
    )
    OR
    (
      transaction_type IN ('TRANSFER', 'REPAYMENT')
      AND account_id IS NULL
      AND from_account_id IS NOT NULL
      AND to_account_id IS NOT NULL
      AND from_account_id <> to_account_id
    )
  ),
  CONSTRAINT ck_transaction_detail_note CHECK (
    note IS NULL OR CHAR_LENGTH(note) <= 100
  ),
  CONSTRAINT ck_transaction_detail_idempotency_key CHECK (
    idempotency_key IS NULL OR CHAR_LENGTH(TRIM(idempotency_key)) > 0
  ),
  CONSTRAINT ck_transaction_detail_deleted CHECK (
    deleted IS NULL OR deleted IN (0, 1)
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单明细主表，保存支出、收入、转账和还款，不保存退款类型';

CREATE TABLE transaction_refund (
  id BIGINT NOT NULL COMMENT '退款记录ID',
  transaction_id BIGINT NOT NULL COMMENT '原始账单明细ID，仅允许关联支出或收入账单',
  refund_no VARCHAR(64) NOT NULL COMMENT '退款记录编号，面向详情和人工核对，必须唯一',
  refund_amount BIGINT NOT NULL COMMENT '本次退款金额，单位为分；每笔记录只保存本次金额',
  idempotency_key VARCHAR(80) NULL COMMENT '创建退款记录的幂等键，在原始账单范围内唯一',
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
  UNIQUE KEY uk_transaction_refund_refund_no (refund_no),
  UNIQUE KEY uk_transaction_refund_transaction_idempotency (transaction_id, idempotency_key),
  KEY idx_transaction_refund_transaction_created (transaction_id, deleted, created_at),
  CONSTRAINT fk_transaction_refund_transaction FOREIGN KEY (transaction_id) REFERENCES transaction_detail (id),
  CONSTRAINT ck_transaction_refund_no CHECK (CHAR_LENGTH(TRIM(refund_no)) > 0),
  CONSTRAINT ck_transaction_refund_amount CHECK (
    refund_amount > 0 AND refund_amount <= 99999999999
  ),
  CONSTRAINT ck_transaction_refund_idempotency_key CHECK (
    idempotency_key IS NULL OR CHAR_LENGTH(TRIM(idempotency_key)) > 0
  ),
  CONSTRAINT ck_transaction_refund_deleted CHECK (
    deleted IS NULL OR deleted IN (0, 1)
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单退款记录关联表，每笔退款单独保存，不作为账单类型';
