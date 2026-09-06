# Verification

- 状态：PASS（代码、构建和浏览器路由回归已验证；未提交注册表单）。
- 根因证据：后端提供 `GET /api/app/auth/password-key`，前端原调用路径多了 `/h5`；H5 启动逻辑无条件重定向登录页。
- 回归证据：直接访问 `http://localhost:5173/#/pages/auth/register/register` 后 URL 保持注册路由，页面显示“账号注册”和“创建你的账号”，浏览器无 error/warn 日志。
- 外部依赖：注册真实提交未执行，未验证真实账号落库和验证码消费流程。
