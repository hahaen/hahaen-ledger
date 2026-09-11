# 执行命令

| 命令 | 结果 |
| --- | --- |
| `server: mvn test` | PASS；37 tests，0 failures/errors/skipped；含头像仅在资料保存时关联的单测。 |
| `app: node --test tests/page-flows.test.mjs` | PASS；25 tests passed，含扩展名/MIME 与真实 JPEG 文件头不一致时按真实类型上传、选择新头像仅暂存至保存的回归。 |
| `app: pnpm run typecheck` | PASS；exit 0。 |
| `app: VITE_API_BASE_URL=http://127.0.0.1:8080 pnpm run build:h5` | PASS；保存后切换头像修复后执行，`DONE Build complete.` |
| `app: VITE_API_BASE_URL=http://127.0.0.1:8080 pnpm run build:mp-weixin` | PASS；保存后切换头像修复后执行，`DONE Build complete.` |

真实 MinIO 联调结果在本文件追加；不记录密码、Token、对象签名 URL 或 MinIO 密钥。
