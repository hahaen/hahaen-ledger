# commands

| 工作目录 | 实际命令/检查 | 结果 |
| --- | --- | --- |
| app | `pnpm run typecheck` | PASS：退出码 0 |
| app | `$env:VITE_API_BASE_URL='http://127.0.0.1:18761'; pnpm run build:h5` | PASS：H5 构建完成；保留既有 Sass legacy-js-api 警告 |
| app | `node tests/visual-server.mjs` | PASS：启动 18761 只读视觉验收服务 |
| 浏览器 | 打开 `http://127.0.0.1:18761/#/pages/index/index` 并读取 DOM/截图 | PASS：摘要卡月份为 2026 年 9 月，金额位于标题下方，支出/收入字段间距收紧 |
