# `h5-mine-page`｜H5 我的页与个人资料闭环

本次迭代完成原型中的 H5“我的”页和“关于与帮助”页，并补齐个人资料读取、按用户创建时间计算累计天数、头像 MinIO 上传、H5 退出登录后留在当前页的交互。

当前范围仅覆盖 H5。微信小程序头像选择和自动登录行为保留现有条件编译入口，后续单独补齐。

状态以 `09-verification.md` 为准；未具备 MySQL、Redis、MinIO 或真实浏览器联调条件的项目分别记录为 BLOCKED 或 NOT_RUN，不将构建结果替代运行证据。

## 文件导航

- `01-requirement.md`：需求、范围和验收条件。
- `02-design.md`：页面、接口和状态设计。
- `03-database.md`：复用现有用户/文件结构及迁移判断。
- `04-api.md`：个人资料接口和头像接口约定。
- `05-backend.md`：Java 实现说明。
- `06-frontend.md`：H5 页面实现说明。
- `07-testing.md`：测试范围和结果。
- `08-commands.md`：实际执行的命令和结果。
- `09-verification.md`：验收状态。
- `10-rollback.md`：回滚策略。
