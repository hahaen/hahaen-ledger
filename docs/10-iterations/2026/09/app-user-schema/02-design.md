# 设计

## 归属模型

```text
app_user
  ├── H5：login_account + password_hash
  └── 微信小程序：user_identity(provider, open_id)
```

`app_user` 保存用户主数据和 H5 认证资料；微信 `open_id` 不直接放入用户主表，避免后续扩展多个身份来源时改变用户主表结构。

## 关键决策

- `login_account`、`password_hash` 允许为空，支持仅使用微信登录的用户。
- `login_account` 使用不包含 `status`、`deleted` 的全表唯一索引，停用或逻辑删除后仍不可复用，避免账号归属歧义。
- `password_hash` 只保存服务端密码哈希，不保存明文密码或前端加密原文。
- `avatar_file_id` 在文件元数据表建立后由后续 V2 Migration 补充外键约束。
- 绑定凭证、会话和验证码不落在用户表，后续使用 Redis/认证模块实现。
- `last_login_ip` 保存最近一次登录 IP；登录历史写入 `app_login_log`。
- 登录日志不保存密码、Token 或微信 `open_id`，失败登录允许 `user_id` 为空。
