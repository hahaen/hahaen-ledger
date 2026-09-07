# 前端核对

## 已确认

- 页面、组件、Store、`utils/api.ts` 和 `utils/file.ts` 的职责符合当前目录规范。
- `pages.json` 底部导航确实只有：首页、日历、资产、我的。
- H5 认证页面调用 `/password-key`、`/captcha`、`/h5/login`、`/h5/register`；保存/刷新路径已有 loading 或错误提示。
- 当前 `pnpm run typecheck`、`pnpm run build:h5`、`pnpm run build:mp-weixin` 均通过。

## 差异

- `ledger.login()` 的小程序分支仍调用 `/api/app/auth/login`，与当前后端不匹配。
- 我的页 H5 支持文件选择，微信分支只提示“后续接入”；不能把头像三态描述成微信已完成。
- 当前工作区未找到文档曾引用的逐页 PNG 证据文件，设计验收应保持 `NOT_RUN`。

