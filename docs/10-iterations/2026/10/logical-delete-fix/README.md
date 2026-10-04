# 账单与账户逻辑删除整改（2026-10-04）

- requirement：删除账单后列表仍可见；排查其他删除入口的相同缺陷。
- design：显式条件逻辑删除，保留归属和删除审计，检查影响行数；账单余额撤销和删除处于原有事务。
- database：无结构变更，不修改历史迁移；不自动修补旧数据和余额，需逐笔核对删除审计与实际账务。
- api：保持 DELETE 既有路径；已删除或他人记录返回不存在，不重复撤销余额。
- backend：修复 transaction_detail、asset_account 的 markDeleted + updateById 组合；核查退款、物品、文件、通知配置和待办。
- frontend：接口保持兼容，无页面变更；后台刷新失败保留旧数据属于既有行为。
- testing：真实 MyBatis SQL 回归、Service 权限/重复删除/影响行数测试、DEV MySQL 事务与查询验证。
- commands：见 [真实命令](08-commands.md)。
- verification：必需验证PASS，客户端与历史数据核对PARTIAL/NOT_RUN，见 [验收](09-verification.md)。
- rollback：回退本轮代码；历史余额需人工审计，不按 deleted_at 自动改数据。

## 同类排查范围

| 入口 | 核查结果 |
| --- | --- |
| 账单删除 | 原 markDeleted + updateById 未落库 deleted，已改显式条件 SQL |
| 账户删除 | 同类缺陷，已修复；历史账单保留 |
| 退款删除 | 已使用 softDeleteById 显式 deleted=1，检查影响行数 |
| 物品删除 | softDelete 显式标记，校验归属/影响行数/删除键 |
| 文件删除 | markDeleted 显式 SQL 含用户与 deleted=0；未发现本次字段排除问题，不代表 MinIO/失败重试完整验收 |
| 通知配置移除 | softDelete 显式标记与归属校验 |
| 待办删除/规则替换 | deleteOne/deletePendingRule 显式标记；规则结束为 active=0 的设计语义 |
| 用户/账本 | 当前未提供用户/账本删除业务接口，无同类入口 |

附带修正 LoggingProfileConfigTest 的 DEV 路径过时断言，使其与现有 application-dev.yml 配置一致，不改变运行配置。
