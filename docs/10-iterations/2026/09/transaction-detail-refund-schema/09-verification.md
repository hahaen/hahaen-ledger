# 验证结论

| 项目 | 状态 | 证据/说明 |
| --- | --- | --- |
| V4 文件名和版本顺序 | PASS | 现有 V1–V3 后新增 `V4__create_transaction_detail_and_refund_tables.sql`。 |
| 两张表和中文注释 | PASS | V4 静态检查；表和字段均有中文 COMMENT。 |
| 主表完整审计字段 | PASS | `transaction_detail` 包含项目要求的 10 个公共字段。 |
| 退款表审计字段分类 | PASS（静态） | `transaction_refund` 有独立编号、金额、幂等和删除生命周期，采用完整 10 个公共审计字段以满足可追溯删除。 |
| 金额精度与原始/有效金额模型 | PASS（静态） | BIGINT 整数分；CHECK 限制金额范围和 `amount <= original_amount`。 |
| 四类字段形状约束 | PASS（静态） | CHECK 限制单账户与转出/转入字段组合。 |
| 单账本字段模型 | PASS（静态） | `transaction_detail` 不保存 `book_id`，通过 `user_id` 归属当前唯一账本。 |
| 唯一编号/索引 | PASS（静态） | 主键、账单编号、退款编号、幂等键和查询索引已声明。 |
| 用户/账户外键 | PASS（静态） | 外键指向现有 `app_user`、`asset_account`；单账本不在账单表重复保存 `book_id`。 |
| 退款累计上限与同事务更新 | NOT_RUN | 需要后续 Service 和 MySQL 运行时验证，本轮不修改业务代码。 |
| MySQL/Flyway 实际执行 | BLOCKED | 本机未监听 MySQL 3306，且未发现 `mysql` 客户端。 |
| `information_schema` 对照 | BLOCKED | 需要成功执行 V4 后验证，当前外部数据库不可用。 |
| Java/前端类型同步 | NOT_RUN | 按用户要求明确不在本次范围。 |
