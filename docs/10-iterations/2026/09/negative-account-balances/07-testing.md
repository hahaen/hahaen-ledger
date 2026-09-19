# 07｜测试

## 计划

- 后端：支出/转账/还款使资金余额变负、信贷支出可超过额度、信贷退款使欠款为负、删除/编辑反向影响。
- 账户：有符号余额可创建/更新，信贷欠款允许超过总额度。
- 资产汇总：正/负资金与正/负信贷余额的资产、负债和净资产分类。
- 前端：账户选择范围、负金额编辑格式、信用卡可用额度超额提示计算及提示不阻止保存；按仓库现有回归、TypeScript、H5/微信构建流程执行可用检查。
- 数据库：可用 MySQL 上执行 Flyway，并核对 migration history 与 check constraints。

## 实际结果

- 后端 `mvn test`：PASS，53 项通过，0 失败。
- 前端 `node --test tests/*.test.mjs`：PASS，56 项通过，0 失败。
- `pnpm run typecheck`：PASS。
- `pnpm run build:h5`、`pnpm run build:mp-weixin`：PASS。
- `git diff --check`：PASS（最终结果记录在 `08-commands.md`）。
- 真实 MySQL/Flyway/information_schema：NOT_RUN；无数据库执行证据。
- DEV API、H5 登录态页面和微信开发者工具/真机：NOT_RUN；构建不等同于运行验收。
