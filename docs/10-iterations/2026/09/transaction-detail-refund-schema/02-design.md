# 设计

采用 `transaction_detail` 单表承载四类账单，使用 `transaction_type` 和三组账户字段表达不同语义；不适用字段由数据库 CHECK 约束为空。

采用 `transaction_refund` 独立关联表，一笔退款一行，通过 `transaction_id` 关联原始账单。退款时间直接使用系统生成的 `created_at`，不重复保存用户输入时间。

金额采用整数分。`original_amount` 永不被退款覆盖，`amount` 为有效金额缓存，`has_refund` 为“曾发生退款”历史标识。退款新增/删除由后续 Service 在锁定原账单的同一事务中维护。

当前没有 `app_book` 表，且产品是单用户、单账本，因此 V4 不在账单表重复保存 `book_id`；未来扩展多账本时再单独设计账本归属模型。
