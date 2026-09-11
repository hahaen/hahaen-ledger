# 验证

PASS（静态核对与回归测试）：H5 应用启动路径不再调用 `ledger.refresh()`；首页自动显示会复用 Store 同月摘要，未命中时才调用 `ledger.refresh()`。同月并发调用复用同一个进行中的 Promise；单次 `ledger.refresh()` 仅在 `Promise.all` 中各发送一条账户和当月摘要请求。Node 回归测试 17/17 通过。

NOT_RUN：使用有效 H5 登录态在浏览器 Network 面板刷新验证；当前本机浏览器未取得可用测试会话。
