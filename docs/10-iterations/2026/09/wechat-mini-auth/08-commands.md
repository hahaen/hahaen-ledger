# 命令

本文件只记录实际执行的命令与结果，不记录任何 Secret、Token 或真实 code。

- `server`: `mvn test`，47 tests，0 failures/errors/skipped，exit 0。
- `app`: `node --test tests/*.test.mjs`，42 tests，0 failures，exit 0。
- `app`: `pnpm run typecheck`，exit 0。
- `app`: `pnpm run build:h5`，构建完成，exit 0。
- `app`: `pnpm run build:mp-weixin`，构建完成，exit 0。
- `server`: 本机运行实例 `POST /api/app/auth/wechat-mini/login` 提交无效 code，返回 HTTP 400、业务错误 `WECHAT_CODE_INVALID`；证明微信响应已被正确解析，未记录 Secret/code。
- `server`: 直接请求微信 `https://api.weixin.qq.com/sns/jscode2session` 提交无效 code，返回 `errcode=40029`；只记录返回类别，不记录 AppSecret。
- `server`: 运行日志证明 MySQL/Flyway V4、Redis PING、MinIO bucket 探针成功。
- `server`: 通过微信开发者工具自动运行构建产物，真实首次登录创建 1 条 `user_identity`；随后重复运行成功登录记录共 2 条且归属同一系统用户。
- `server`: 仅删除该临时开发会话的 Redis Token 映射后再次自动运行，成功登录记录增至 3 条且仍归属同一系统用户；用新 Token 访问 `/api/app/accounts` 和 `/api/app/user/profile` 均 HTTP 200。
- `微信开发者工具`: `cli.bat islogin` 返回 `{"login":true}`；`open`、`auto --trust-project` 成功加载 `app/dist/build/mp-weixin`，未输入账号、密码或 Secret。
- `git diff --check`，exit 0。
