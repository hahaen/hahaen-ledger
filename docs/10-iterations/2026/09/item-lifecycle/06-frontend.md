# 前端

items 一级页、item-detail 二级页，API方法集中 api.ts、物品类型和输入校验独立工具。独立数据状态，不触碰 ledger 账户与账单状态。创建/退役/删除保存loading、禁用、失败保留数据及同一幂等键重试；成功后刷新。图表使用跨平台uni canvas。

详情onLoad确定有效ID后加载；onShow在已有有效ID且不加载时刷新，避免首次重复请求，并在返回前台/跨自然日时更新成本。canvas监听窗口resize并在组件销毁时注销。
