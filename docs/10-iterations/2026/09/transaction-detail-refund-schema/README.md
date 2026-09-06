# `transaction-detail-refund-schema`｜账单明细与退款数据库设计

## 迭代目标

为支出、收入、转账、还款建立账单明细主表，并以独立关联表支持支出/收入的多笔退款、逻辑删除和有效金额口径。

## 本次范围

- 新增 Flyway V4：`transaction_detail`、`transaction_refund`。
- 新增当前数据库规范文档：`docs/02-database/transaction-detail-refund.md`。
- 不修改 Java、前端、TypeScript 或业务接口实现。

## 重要前提

当前仓库没有 `app_book` 表，账户表也按单用户、单账本模型设计。V4 不在 `transaction_detail` 重复保存 `book_id`，账单通过 `user_id` 归属当前用户及其唯一账本；归属、账户类型、事务和退款业务规则由后续 Service 实现。

## 状态

静态文件和规则核对以 `09-verification.md` 为准。MySQL/Flyway 运行时证据必须按实际环境记录为 PASS、BLOCKED 或 NOT_RUN。

## 文件导航

`01-requirement.md` → `02-design.md` → `03-database.md` → `04-api.md` → `05-backend.md` → `06-frontend.md` → `07-testing.md` → `08-commands.md` → `09-verification.md` → `10-rollback.md`
