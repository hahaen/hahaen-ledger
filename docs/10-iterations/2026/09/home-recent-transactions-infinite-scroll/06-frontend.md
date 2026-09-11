# 前端

Store 增加 `loadHomeRecentTransactions(beforeMonth?)`。首页将列表加载状态、摘要加载状态和加载更多失败状态分开；记录容器监听触底事件，在未加载且 `hasMore=true` 时以 `startMonth` 取下一段，按 ID 去重追加，并显示加载中、结束和失败重试提示。内容不足以滚动时，底部“加载更多账单”按钮使用同一游标作为回退入口。
