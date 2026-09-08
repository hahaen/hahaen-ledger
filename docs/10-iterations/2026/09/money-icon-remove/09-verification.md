# 验证

验证结果：

- 静态检索：PASS，app/src 无 ¥、￥、money-currency、currency 类金额图标引用。
- 类型检查：PASS，pnpm run typecheck。
- H5 构建：PASS，使用 VITE_API_BASE_URL=http://127.0.0.1:8080。
- 微信小程序构建：PASS，使用 VITE_API_BASE_URL=http://127.0.0.1:8080。
- 差异空白检查：PASS，git diff --check。
- 只读视觉验收：PASS，首页、资产页、账户详情和账单详情的可访问文本均只保留金额数值；未出现 ¥。

真实微信开发者工具和真实业务会话不在本次环境中，若未连接则记录为 NOT_RUN 或 BLOCKED，不冒充 PASS。
