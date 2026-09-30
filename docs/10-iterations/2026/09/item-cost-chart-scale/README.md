# 物品日均成本长期走势优化

## Requirement

长期持有物品的日均成本图被首日高值压成贴底直线，需要更清晰地展示全周期变化，保留真实金额和现有详情视觉。

## Design

金额绝对值跨度达到 20 倍且最高值至少 1 元时，纵轴使用带符号 log1p 非等距刻度（平滑尺度为 1 元），避免零和负值无法显示。其余使用线性刻度；横轴仍按实际服役天数分布。非等距模式显示说明，刻度标签始终还原为真实元金额。

## Database / API / Backend

无变更。不改变日均成本计算、后端最多 31 点取样、退役终点及权限。无需迁移或后端构建。

## Frontend

ItemCostChart 自动选择纵轴，使用五条网格帮助读取长期走势；延续绿色折线和端点。跨年日期显示年份，单点只显示一个日期。

## Testing

定向覆盖长期曲线可见度、普通线性、零成本、单点、退役负值；执行物品回归、类型检查和 H5/微信构建；运行 H5 只读夹具检查布局。

## Commands / Verification

PASS：`node --test app/tests/items.test.mjs app/tests/item-cost-chart.test.mjs` 9/9；`pnpm run typecheck`、`pnpm run build:h5`、`pnpm run build:mp-weixin` 均退出 0。

PASS（只读 H5 夹具）：`node tests/visual-server.mjs`，浏览器检查 201 天在役走势、365 天退役负成本和跨年日期；375px 目视曲线、标签及底部区域，320px 实测 document.scrollWidth=innerWidth=320，无横向溢出。截图：[在役](evidence/h5-375.png)、[退役](evidence/h5-retired-375.png)。

PARTIAL：运行视觉证据为独立只读示例，不证明真实登录态接口。NOT_RUN：微信模拟器与真机视觉验收。本次没有改变平台 API、业务金额、数据库或权限；真实服务写入与后端测试不适用。构建仍有既有 Sass legacy-js-api 提示。

`git pull --ff-only` 返回 Already up to date；定向 stash 恢复后全部任务文件逐字节核对一致；`git diff --check` 退出 0。远端未改变相关代码，构建产物对应最终代码。同步后复跑物品回归和类型检查 PASS。

## Rollback

恢复 ItemCostChart 和移除新增坐标工具即可，无数据回滚。
