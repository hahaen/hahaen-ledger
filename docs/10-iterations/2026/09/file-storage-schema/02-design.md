# 设计

## 存储模型

```text
app_user 1 ── N app_file
app_user 1 ── 0..1 app_file（头像）
账本/账单 ── app_file（账单附件，待对应业务表生成后补充外键）
```

## 关键决策

- 数据库只保存 `storage_provider`、`bucket_name`、`object_key`。
- 不保存永久 URL 或永久签名 URL；预览时由后端通过 `MinioStorageService` 生成短时效地址。
- `user_id` 是必填归属字段，业务层必须再次校验账本和账单归属，防止 IDOR。
- `AVATAR` 只能归属用户，`TRANSACTION_ATTACHMENT` 必须同时具备账本和账单 ID。
- `status` 表示上传生命周期，`deleted` 表示数据库逻辑删除，二者不互相替代。
- `idempotency_key` 用于上传/确认操作防重复创建。
