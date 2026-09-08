# 命令记录

以下命令已在实现后执行：

- `pnpm run typecheck`：exit 0
- `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5`：`DONE Build complete.`
- `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:mp-weixin`：`DONE Build complete.`
- 本机只读视觉服务 `node tests/visual-server.mjs`：启动于 `http://127.0.0.1:18761`
- 浏览器只读计算样式核对：4 个导航项均 103px，按钮无边框、无圆角、无阴影
