# 命令与真实结果

## 实际命令

| 命令 | 结果 |
| --- | --- |
| `pnpm exec node --test tests/*.test.mjs`（`app/`） | `PASS`，53 tests passed |
| `pnpm run typecheck`（`app/`） | `PASS`，exit 0 |
| `pnpm run lint`（`app/`） | `BLOCKED`，`[ERR_PNPM_NO_SCRIPT] Missing script: lint` |
| `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5`（`app/`） | `PASS`，`DONE Build complete.` |
| `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:mp-weixin`（`app/`） | `PASS`，`DONE Build complete.` |
| `git diff --check` | `PASS` |

构建使用本地 API 地址仅用于编译，不代表真实后端、登录或部署验收；未记录任何凭证。

本次补充核对微信产物：`profile.wxml` 中保存按钮节点存在，`app.wxss` 中个人中心保存/修改密码按钮使用明确背景色并清理 `button::after`；产物检查不等同于微信开发者工具运行验收。

本次同时核对头像产物：`profile.js` 使用 `chooseImage`，`utils/file.js` 使用临时文件读取和预签名 PUT；未发现 `getUserProfile` 或 `getUserInfo`。

本次故障修复后再次核对：`utils/file.js` 使用 `getFileSystemManager().getFileInfo` 读取大小、由同一二进制内容计算 SHA-256 并使用 `dataType: 'text'` 进行 PUT，未发现已废弃的 `uni.getFileInfo`。

本次新增记账页核对：`entry.wxml` 的 `screen-nav` 位于 `entry-content` 之前；`app.wxss` 已生成 MP 专用 44px 导航预留、`.entry-type button` 的 38px 高度/最小高度和单倍行高。
