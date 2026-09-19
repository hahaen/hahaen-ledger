# Design

保留 `.entry-content` 独立滚动和 `.keypad` 固定定位。在 `#ifdef H5` 条件编译样式中为 `.entry-content` 添加 `-webkit-overflow-scrolling: touch`，启用 iOS Safari 的惯性触摸滚动；不改变小程序样式和滚动结构。
