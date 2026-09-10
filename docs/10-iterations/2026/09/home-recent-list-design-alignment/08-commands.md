# commands

| 工作目录 | 实际命令/检查 | 结果 |
| --- | --- | --- |
| app | `pnpm run typecheck` | PASS：退出码 0 |
| app | `$env:VITE_API_BASE_URL='http://127.0.0.1:18761'; pnpm run build:h5` | PASS：H5 构建完成；保留既有 Sass legacy-js-api 警告 |
| 浏览器 | 打开 `http://127.0.0.1:18761/#/pages/index/index` 并读取 DOM/截图 | PASS：最近记账区域与设计稿的日期行和底色对齐 |
