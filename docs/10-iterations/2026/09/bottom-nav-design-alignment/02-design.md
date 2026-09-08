# 设计

在公共 `prototype.scss` 中为 `.nav-item` 显式定义 `flex: 1 1 0` 和 `width: 25%`，并清除 `border`、`border-radius`、`box-shadow`、默认外观、默认内边距、背景和 `::after` 伪元素；`.bottom-nav` 改用等宽布局并移除整体外围边框，导航图标与文字组向下做 3px 的视觉居中修正，并对不同 Unicode 字形做视觉尺寸补偿，使其与首页图标观感一致，不改变导航容器的安全区高度。
