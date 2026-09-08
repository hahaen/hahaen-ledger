# 命令

- app：pnpm run typecheck，exit 0。
- app：临时设置 VITE_API_BASE_URL=http://127.0.0.1:8080 后执行 pnpm run build:h5，输出 DONE Build complete.，exit 0。
- app：临时设置 VITE_API_BASE_URL=http://127.0.0.1:8080 后执行 pnpm run build:mp-weixin，输出 DONE Build complete.，exit 0。
- 视觉服务：node tests/visual-server.mjs，只读服务监听 http://127.0.0.1:18761。
- 视口核对：320px、375px、414px 均无横向溢出；金额组件计算得到货币符号位于数值右侧。
