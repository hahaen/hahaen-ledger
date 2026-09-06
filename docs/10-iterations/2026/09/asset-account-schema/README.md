# `asset-account-schema`｜资产账户表设计

本次迭代根据《哈记账小程序_原型设计稿》资产页面的资金账户和信贷账户设计，新增 Flyway V3 资产账户表。采用统一 `asset_account` 表，以 `account_type` 区分 `FUND` 资金账户和 `CREDIT` 信贷账户，并用数据库检查约束维护两类账户的金额字段语义。

## 范围

- 新增 `V3__create_asset_account_table.sql`。
- 新增当前数据库设计说明并更新数据库文档索引、月度迭代索引、阶段审计记录。
- 不修改 Java Entity、DTO、VO、Mapper、Service、Controller，不修改 TypeScript、API 或页面，不实现账户业务。

## 当前结论

- SQL 与文档静态核对：PASS。
- 历史 V1/V2 Migration：未修改。
- Flyway/MySQL 实际执行与 `information_schema` 对照：NOT_RUN；本轮只提交迁移文件和文档。

## 文件导航

`01` 需求来源与边界；`02` 统一表设计与替代方案；`03` 数据库字段、索引、约束与风险；`04` API 影响；`05` 后端影响；`06` 前端影响；`07` 测试计划与结果；`08` 实际命令；`09` 验证结论；`10` 回滚策略。
