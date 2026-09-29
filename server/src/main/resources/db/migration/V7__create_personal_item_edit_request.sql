-- 保存物品资料编辑的幂等请求，确保后续编辑不会覆盖早期请求指纹。
CREATE TABLE personal_item_edit_request (
  user_id BIGINT NOT NULL COMMENT '所属用户ID',
  idempotency_key VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '资料编辑请求幂等键，用户内唯一',
  item_id BIGINT NOT NULL COMMENT '被编辑物品ID',
  request_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '标准化编辑请求SHA256摘要',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  deleted TINYINT NULL DEFAULT 0 COMMENT '删除标识，0存在1删除',
  PRIMARY KEY (user_id, idempotency_key),
  KEY idx_item_edit_request_item (item_id),
  CONSTRAINT fk_item_edit_request_user FOREIGN KEY (user_id) REFERENCES app_user (id),
  CONSTRAINT fk_item_edit_request_item FOREIGN KEY (item_id) REFERENCES personal_item (id),
  CONSTRAINT ck_item_edit_request_deleted CHECK (deleted IS NULL OR deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='个人物品资料编辑幂等请求关系表';
