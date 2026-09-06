# 命令

| 命令 | 结果 |
| --- | --- |
| `rg --files -g '!node_modules' -g '!target' -g '!dist'` | PASS，确认原型和文档位置 |
| `Get-Content` 静态复核原型、认证实现和文档 | PASS |
| 本机浏览器原型交互复验 | PASS，覆盖登录/注册/图形验证码/失败/禁用/成功/退出/微信自动登录 |
| 320px、375px、414px 精确视口复验 | NOT_RUN，当前浏览器未提供 viewport 覆盖能力 |
| 真实 H5 账号认证联调 | BLOCKED，当前无对应后端接口 |
