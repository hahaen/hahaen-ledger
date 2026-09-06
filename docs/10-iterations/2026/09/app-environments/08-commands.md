# 命令证据

- `pnpm run typecheck`：通过，退出码 0。
- `pnpm exec uni build -p h5 --mode development`：通过，输出 `DONE Build complete.`。
- `pnpm run dev:h5`：通过，Vite 输出本地地址 `http://localhost:5173/`；随后主动停止开发服务器。
- `pnpm run build:h5`（临时注入 `https://api.example.com`，不落盘）：通过，输出 `DONE Build complete.`。
- `pnpm run build:mp-weixin`（临时注入 `https://api.example.com`，不落盘）：通过，输出 `DONE Build complete.`。
- `pnpm run build:h5`（未配置 API 地址）：按预期失败，提示生产构建缺少 `VITE_API_BASE_URL`。

本文件不得记录真实凭证、Token 或永久签名 URL。
