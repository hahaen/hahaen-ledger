-- 保存重新服役请求，防止旧请求在再次退役后重放。
CREATE TABLE personal_item_reactivate_request (
  user_id BIGINT NOT NULL COMMENT '所属用户ID',
  idempotency_key VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '重新服役请求幂等键，用户内唯一',
  item_id BIGINT NOT NULL COMMENT '重新服役物品ID',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  deleted TINYINT NULL DEFAULT 0 COMMENT '删除标识，0存在1删除',
  PRIMARY KEY (user_id, idempotency_key),
  KEY idx_item_reactivate_request_item (item_id),
  CONSTRAINT fk_item_reactivate_request_user FOREIGN KEY (user_id) REFERENCES app_user (id),
  CONSTRAINT fk_item_reactivate_request_item FOREIGN KEY (item_id) REFERENCES personal_item (id),
  CONSTRAINT ck_item_reactivate_request_deleted CHECK (deleted IS NULL OR deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='个人物品重新服役幂等请求关系表';
