# 命令

以下命令均在本地工作区实际执行，未记录密码、token、Cookie、私钥或永久签名 URL。

| 命令 | 结果 |
| --- | --- |
| `node --test tests/auth-guard.test.mjs`（`app/`） | PASS，5/5 通过，exit 0。 |
| `pnpm run typecheck`（`app/`） | PASS，exit 0。 |
| 临时注入 `VITE_API_BASE_URL=http://127.0.0.1:8080` 后执行 `pnpm run build:h5`（`app/`） | PASS，`DONE Build complete.`，exit 0；命令结束后已移除临时环境变量。 |
| `git diff --check`（仓库根目录） | PASS，exit 0；仅有既有 CRLF/LF 转换提示，无空白错误。 |
