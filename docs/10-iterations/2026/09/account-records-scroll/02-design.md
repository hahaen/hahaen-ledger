# design

账户详情页采用纵向 flex 视口容器。导航、账户卡片、流水标题和筛选行设置 `flex-shrink:0`；记录区使用 `scroll-view scroll-y`、`flex:1`、`min-height:0`，日期标题位于记录滚动容器内部，并使用 `position:sticky; top:0` 吸顶，直到下一日期分组替换。
