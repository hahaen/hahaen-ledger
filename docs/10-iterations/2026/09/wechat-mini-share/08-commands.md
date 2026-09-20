# 命令与结果

执行目录均为 `app/`。本文件只记录本轮实际执行的命令和结果，不记录凭证、Token、密钥或永久签名 URL。

- `pnpm exec node --test tests/wechat-share.test.mjs`：PASS，15/15。
- `pnpm exec node --test tests/*.test.mjs`：PARTIAL，75/77；2 项为工作区既有 mine 模板断言与当前 tap-feedback 标记不匹配。
- `pnpm run typecheck`：PASS，`vue-tsc --noEmit` exit 0。
- `pnpm run build:h5`：PASS，输出 `DONE Build complete.`。
- `pnpm run build:mp-weixin`：PASS，输出可导入微信开发者工具的 `dist/build/mp-weixin`；产物中 14 个页面均引用统一转发模块。
- `pnpm run lint`：BLOCKED，`ERR_PNPM_NO_SCRIPT`，当前项目未配置 lint script。
- `git diff --check`：PASS（仅报告工作区既有 CRLF→LF warning，无 whitespace error）。
