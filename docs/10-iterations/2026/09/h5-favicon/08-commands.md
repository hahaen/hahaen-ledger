# 命令与结果

本文件在验证完成后记录本次实际执行的命令和结果，不记录任何凭证或 Token。

在 `app` 目录实际执行：

- `pnpm run typecheck`：PASS，`vue-tsc --noEmit` exit 0。
- `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5`：PASS，`DONE Build complete.`。
- 检查 `dist/build/h5/index.html` 和 favicon 资源：PASS；生产入口包含经构建指纹化的 favicon，构建图标为 1254x1254 PNG，四角 alpha 为 0。
- 浏览器打开 `http://127.0.0.1:5173/`：PASS；入口 DOM 的 favicon 为 `/static/brand.png`、类型为 `image/png`，该资源返回 HTTP 200。

构建过程仅输出 uni-app 更新提示和 Sass legacy API 弃用提示；未记录任何敏感配置值。
