# 设计

- 使用 Vite 的 `development` 和 `production` mode。
- 通过 `VITE_API_BASE_URL` 注入 API 根地址，通过 `VITE_APP_ENV` 标识当前环境。
- 在 `vite.config.ts` 中统一读取和校验环境变量；生产构建缺少 API 地址时直接失败，避免产物误连本机。
- `src/utils/api.ts` 只消费集中配置，并统一去除根地址末尾斜杠。
- `app/.env.*` 作为本地配置忽略，仓库保留 `.env.*.example` 示例。
