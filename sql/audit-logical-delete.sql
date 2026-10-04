-- 只读人工审计：疑似曾写入删除审计但逻辑删除标记未生效的记录。
-- deleted_at 非空不能证明删除次数或余额应修正多少，禁止据此自动更新余额。
SELECT id, user_id, deleted, deleted_at, deleted_by
FROM transaction_detail WHERE deleted = 0 AND deleted_at IS NOT NULL;
SELECT id, user_id, deleted, deleted_at, deleted_by
FROM asset_account WHERE deleted = 0 AND deleted_at IS NOT NULL;
