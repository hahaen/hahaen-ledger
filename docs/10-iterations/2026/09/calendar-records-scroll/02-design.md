# design

日历页根容器采用 `display:flex`、`height:100vh`、`overflow:hidden`；页头、月历卡片和日期标题设置 `flex-shrink:0`。选中日期的记录使用 `scroll-view scroll-y`，通过 `flex:1` 和 `min-height:0` 占据剩余空间。
