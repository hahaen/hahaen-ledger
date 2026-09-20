# Design

H5 记账页保持动态视口高度和固定数字键盘，在 H5 条件编译区明确 `.entry-content` 为 `flex:1 1 0`、`height:0`、`min-height:0`、`overflow-y:auto` 的独立滚动容器，并启用 iOS 惯性滚动。微信小程序不进入该条件编译分支，继续使用现有布局。
