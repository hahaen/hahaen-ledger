# `default-mini-home`｜微信小程序默认进入首页

本次迭代将微信小程序登录成功后的默认落点统一为首页，不再由启动流程强制进入 `pages/first-use/first-use`。首次使用页文件仍保留，可作为后续显式入口使用。

状态以 `09-verification.md` 为准。本次只涉及小程序入口路由和启动跳转，不修改后端登录、用户、Token 或数据库结构。

## 文件导航

- `01-requirement.md`：需求和验收条件。
- `02-design.md`：入口路由设计。
- `03-database.md`：数据库影响。
- `04-api.md`：API 影响。
- `05-backend.md`：后端影响。
- `06-frontend.md`：前端修改。
- `07-testing.md`：测试范围。
- `08-commands.md`：实际命令和结果。
- `09-verification.md`：验证结论。
- `10-rollback.md`：回滚策略。
