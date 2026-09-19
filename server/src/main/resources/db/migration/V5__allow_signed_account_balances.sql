-- 允许资金账户及信贷账户欠款使用有符号金额；超出信用卡额度由记账页提示，不作为保存限制。
ALTER TABLE asset_account
  DROP CHECK ck_asset_account_amounts_non_negative,
  DROP CHECK ck_asset_account_type_amounts,
  ADD CONSTRAINT ck_asset_account_signed_amounts CHECK (
    total_limit_cent IS NULL OR total_limit_cent >= 0
  ),
  ADD CONSTRAINT ck_asset_account_type_amounts CHECK (
    (account_type = 'FUND' AND balance_cent IS NOT NULL AND total_limit_cent IS NULL AND current_debt_cent IS NULL)
    OR
    (account_type = 'CREDIT' AND balance_cent IS NULL AND total_limit_cent IS NOT NULL AND current_debt_cent IS NOT NULL)
  );
