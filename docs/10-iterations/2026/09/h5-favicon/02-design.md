# 设计

在 `app/index.html` 的 `<head>` 中增加标准 PNG favicon 声明：`/static/brand.png`。该路径对应 uni-app 的静态资源输出路径，开发和生产 H5 均可复用。主图标资源使用带 alpha 通道的透明 PNG，去除原有白色圆角底，保留品牌主体。
