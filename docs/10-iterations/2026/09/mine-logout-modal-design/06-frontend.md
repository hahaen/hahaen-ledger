# 前端

`app/src/pages/mine/mine.vue` 增加 `logoutOpen` 状态，将设置项点击改为仅打开确认弹层。弹层复用 `asset-create-backdrop`、`asset-create-handle`、`account-delete-modal` 和 `account-delete-*`，包含标题、原有业务说明、取消和退出登录按钮。

`confirmLogout()` 仅在确认后调用既有 `ledger.logout()`；退出期间锁定遮罩关闭和两个按钮，并展示“退出中…”。无论注销请求响应如何，`logout()` 都会清理本地会话，页面随后重置头像和资料、关闭弹层；正常响应继续展示“已退出登录”提示。
