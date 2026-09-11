# 数据库

无 Migration、表、字段或索引变化。查询复用 `transaction_detail` 的 `user_id`、`deleted`、`occurred_at` 条件，并按 `occurred_at DESC, id DESC` 稳定排序。
