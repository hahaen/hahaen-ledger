# 待办修改规则后详情失效修复（2026-10-01）

- requirement：修复从详情修改规则后出现“待办不存在，请刷新”。
- design：保存成功返回并刷新清单，取消编辑仍可返回原详情；不改变规则替换发生项和历史快照语义。
- database：无 Schema、迁移或数据修补。
- api：保持 PUT 返回空值；编辑替换未完成项，原 occurrenceId 失效。
- backend：保持现有归属、事务、幂等和逻辑删除行为。
- frontend：保存完成跳过旧详情；无清单页面栈时重定向清单。
- testing：先用实际 Vue 脚本模拟编辑替换发生项及返回刷新，捕获用户报错；覆盖取消、失败重试、防重及缺少清单栈。
- commands：见 [真实执行记录](08-commands.md)。
- verification：必需验证 PASS，客户端完整验收 PARTIAL，补充真机验证 NOT_RUN；见 [验收](09-verification.md)。
- rollback：回退本次前端与测试文档提交，无数据库回滚。
