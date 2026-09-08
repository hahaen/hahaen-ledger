# 验证

| 检查项 | 实际证据 | 状态 |
| --- | --- | --- |
| 前端类型检查 | `app` 执行 `pnpm run typecheck`，exit 0 | PASS |
| H5 生产构建 | 配置本地构建 API 地址后执行 `pnpm run build:h5`，`DONE Build complete.` | PASS |
| 微信小程序生产构建 | 配置本地构建 API 地址后执行 `pnpm run build:mp-weixin`，`DONE Build complete.` | PASS |
| 导航按钮计算样式 | 本机只读视觉服务资产页：4 个 `.nav-item` 宽度均为 103px，`flex=1 1 0px`、`border=0`、`border-radius=0`、`box-shadow=none`、背景透明 | PASS |
| 设计稿视觉 spot check | 本机只读视觉服务 `http://127.0.0.1:18761/#/pages/assets/assets` 页面截图已核对，底部为连续白色栏，资产选中态保留 | PARTIAL（截图未持久化到 `app/tests/evidence/`） |

MySQL、Redis、MinIO 和微信开发者工具不属于本次 CSS 修改范围，未执行真实联调。
