# 命令记录

在 `app` 目录实际执行：

- `pnpm typecheck`：PASS，`vue-tsc --noEmit` 正常结束。
- `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm build:h5`：PASS，输出 `DONE Build complete.`；环境变量仅用于本次命令，未写入配置文件。
- `git diff --check`：PASS，无空白错误。

未记录任何密码、Token、AppSecret、Access Key 或其他敏感配置值。
