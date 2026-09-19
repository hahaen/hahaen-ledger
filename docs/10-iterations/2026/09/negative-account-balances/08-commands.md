# 08｜实际命令

实现前：已读取项目说明、数据库/API 规范、账户/账单实现与相关测试；已检查 `git status --short` 并发现工作区有其他未提交改动，未覆盖这些文件。

在 `server/` 执行 `mvn test`：退出码 0；53 项测试通过、0 失败。

在 `app/` 执行 `node --test tests/*.test.mjs`：退出码 0；56 项测试通过、0 失败。

在 `app/` 执行 `pnpm run typecheck`：退出码 0。

在 `app/` 执行 `pnpm run build:h5` 与 `pnpm run build:mp-weixin`：均退出码 0，构建完成。

在仓库根目录执行 `git diff --check`：退出码 0；若出现 Git 的 CRLF 行尾提示，仅为提示，不是空白错误。

未执行 Flyway 启动、MySQL 查询、DEV 接口请求、H5 登录态操作或微信开发者工具/真机验收。

2026-09-19 额度提示扩展复核：

- `server/` 执行 `mvn test`：退出码 0；53 项通过、0 失败。覆盖信贷支出超额度、账户创建/更新欠款超额度和 V5 约束静态核对。
- `app/` 执行 `node --test tests/*.test.mjs`：退出码 0；56 项通过、0 失败。覆盖可用额度超额计算、提示文案及记账保存流程不被拦截。
- `app/` 执行 `pnpm run typecheck`：退出码 0。
- `app/` 执行 `pnpm run build:h5` 与 `pnpm run build:mp-weixin`：均退出码 0，构建完成。
- 未执行 Flyway/MySQL 迁移、DEV API、H5 登录态页面或微信开发者工具/真机验收。
