# design

复用账户详情页的 `asset-create-backdrop`、`asset-create-handle` 和 `account-delete-*` 视觉规格，使用自定义弹层替代 `uni.showModal`。弹层打开与实际删除分离，确认按钮负责触发既有 API 调用，删除中禁用操作并保留失败重试能力。

