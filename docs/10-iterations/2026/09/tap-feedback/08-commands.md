# 命令与结果

执行目录均为 `app/`：

- `pnpm exec node --test tests/*.test.mjs`：PASS，60/60。
- `pnpm run typecheck`：PASS。
- `pnpm run build:h5`：PASS。
- `pnpm run build:mp-weixin`：PASS。
- `git diff --check`：PASS（仅有既有 CRLF→LF warning，无 whitespace error）。

本文件不记录任何凭证、Token、密钥或真实服务地址。
