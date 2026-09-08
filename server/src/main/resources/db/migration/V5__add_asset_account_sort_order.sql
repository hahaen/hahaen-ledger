cd-- 为资产账户增加同类型展示顺序；旧数据按原有名称、ID顺序初始化。

ALTER TABLE asset_account
  ADD COLUMN sort_order INT NOT NULL DEFAULT 0 COMMENT '同类账户展示顺序，数字越小越靠前' AFTER account_type;

UPDATE asset_account account
JOIN (
  SELECT id, ROW_NUMBER() OVER (PARTITION BY user_id, account_type ORDER BY account_name ASC, id ASC) AS new_sort_order
  FROM asset_account
  WHERE deleted = 0
) ranked ON ranked.id = account.id
SET account.sort_order = ranked.new_sort_order;

ALTER TABLE asset_account
  MODIFY COLUMN sort_order INT NOT NULL COMMENT '同类账户展示顺序，数字越小越靠前',
  ADD KEY idx_asset_account_user_type_deleted_order (user_id, account_type, deleted, sort_order, id);
