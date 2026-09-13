# `h5-authenticated-entry-home`｜H5 已登录默认入口回首页

本次迭代修复 H5 用户关闭页面后重新打开仍停留在登录页的问题：本地恢复到有效 token 后，默认根路径、登录页和注册页统一进入首页。

状态以 `09-verification.md` 为准。真实浏览器关闭后重新打开必须使用有效 H5 会话实际验收。

## 文件导航

- `01-requirement.md`：需求和验收条件。
- `02-design.md`：路由分流设计。
- `03-database.md`：数据库影响。
- `04-api.md`：API 影响。
- `05-backend.md`：后端影响。
- `06-frontend.md`：前端实现。
- `07-testing.md`：测试范围。
- `08-commands.md`：真实执行命令和结果。
- `09-verification.md`：最终验证状态。
- `10-rollback.md`：回滚策略。
