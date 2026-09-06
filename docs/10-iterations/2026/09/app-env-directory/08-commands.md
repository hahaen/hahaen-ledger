# 实际命令

以下命令于 2026-09-06 在 app 目录实际执行：

| 命令/操作 | 实际结果 |
| --- | --- |
| `pnpm --version`（仓库根目录） | 11.22.0，exit 0 |
| `pnpm run typecheck` | exit 0 |
| PowerShell here-string 经 `node` 调用 Vite `loadEnv` 与断言 | 通用变量在两种 mode 可见、环境覆盖通用、开发/生产隔离、进程变量优先，exit 0；临时探针已恢复 |
| `pnpm exec uni build -p h5 --mode development` | DONE，exit 0 |
| `pnpm run build:h5`（生产 API 留空） | exit 1，提示配置 app/env/.env.production，符合拦截预期 |
| 临时向 `env/.env.production` 写入公开示例地址后执行 `pnpm run build:h5` | DONE，exit 0 |
| 同一临时配置执行 `pnpm run build:mp-weixin` | DONE，exit 0 |
| PowerShell 检查两个平台 JS 产物包含示例 API 地址 | PASS，exit 0；finally 按原始字节恢复生产环境文件 |

生产构建验证使用 `https://api.example.com`，仅核对配置注入，不请求该域名。构建仍有既有 Sass legacy-js-api 弃用提示。
