# `h5-favicon`｜H5 浏览器主应用图标

## 迭代目的

让 H5 浏览器标签页持续使用哈记账主应用图标，与登录页和应用内品牌展示保持一致。

## 范围

- 入口文件：`app/index.html`。
- 复用资源：`app/static/brand.png`。
- 不涉及数据库、API、后端、认证、权限或业务页面。

## 交付物

- `01-requirement.md`：需求与约束。
- `02-design.md`：实现设计。
- `03-database.md`：数据库影响。
- `04-api.md`：API 影响。
- `05-backend.md`：后端影响。
- `06-frontend.md`：前端改动。
- `07-testing.md`：测试范围。
- `08-commands.md`：真实执行命令。
- `09-verification.md`：验证结论。
- `10-rollback.md`：回滚方式。

状态以 `09-verification.md` 为准；浏览器缓存可能需要强制刷新后才显示新图标。
