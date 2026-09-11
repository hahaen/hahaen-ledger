# 测试

PASS：前端类型检查、H5 生产构建，以及 `node --test tests/page-flows.test.mjs`（17/17）。回归场景校验 H5 启动代码不调用 `ledger.refresh()`，首页自动显示复用同月摘要，并校验同月并发刷新只发出一条账户请求和一条摘要请求。

NOT_RUN：有效登录态浏览器 Network 面板验收；当前未提供可用于本机验证的测试会话。
