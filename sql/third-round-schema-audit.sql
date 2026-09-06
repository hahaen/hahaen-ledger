-- 只读人工审计 SQL；不要在生产直接执行写操作。
SELECT table_name, table_comment
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('app_user','user_identity','ledger_book','ledger_account','ledger_transaction','transaction_refund')
ORDER BY table_name;

SELECT table_name, column_name, column_type, is_nullable, column_default, column_comment
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name IN ('app_user','user_identity','ledger_book','ledger_account','ledger_transaction','transaction_refund')
ORDER BY table_name, ordinal_position;

-- 当前 V1 公共字段约束检查；正常结果每行均为 PASS。
-- 此查询核对字段定义，不把 information_schema 行数误当成业务 NULL 数量。
SELECT table_name, column_name, is_nullable, column_default,
  CASE
    WHEN column_name = 'created_at'
      AND is_nullable = 'NO'
      AND UPPER(column_default) = 'CURRENT_TIMESTAMP(3)' THEN 'PASS'
    WHEN column_name = 'deleted'
      AND is_nullable = 'YES' AND column_default = '0' THEN 'PASS'
    WHEN column_name NOT IN ('created_at', 'deleted')
      AND is_nullable = 'YES' AND column_default IS NULL THEN 'PASS'
    ELSE 'FAIL'
  END AS audit_constraint_status
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name IN ('app_user','user_identity','ledger_book','ledger_account','ledger_transaction','transaction_refund')
  AND column_name IN ('created_at','created_by','created_name','updated_at','updated_by','update_name',
                      'deleted_at','deleted_by','deleted_name','deleted')
ORDER BY table_name, ordinal_position;

-- 五张主表应各有 10 列；关联表含历史 updated_at 共 3 列，Entity 仅使用其中 2 列。
SELECT table_name, COUNT(*) AS audit_column_count,
  CASE WHEN COUNT(*) = IF(table_name = 'user_identity', 3, 10) THEN 'PASS' ELSE 'FAIL' END AS audit_column_status
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name IN ('app_user','user_identity','ledger_book','ledger_account','ledger_transaction','transaction_refund')
  AND column_name IN ('created_at','created_by','created_name','updated_at','updated_by','update_name',
                      'deleted_at','deleted_by','deleted_name','deleted')
GROUP BY table_name;
