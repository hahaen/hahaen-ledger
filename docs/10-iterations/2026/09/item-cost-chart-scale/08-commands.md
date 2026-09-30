# Commands / Verification

PASS：`node --test app/tests/items.test.mjs app/tests/item-cost-chart.test.mjs` 9/9；`pnpm run typecheck`、`pnpm run build:h5`、`pnpm run build:mp-weixin` 均退出 0。

PASS（只读 H5 夹具）：`node tests/visual-server.mjs`，浏览器检查 201 天在役走势、365 天退役负成本和跨年日期；375px 目视曲线、标签及底部区域，320px 实测 document.scrollWidth=innerWidth=320，无横向溢出。截图：[在役](evidence/h5-375.png)、[退役](evidence/h5-retired-375.png)。

PARTIAL：运行视觉证据为独立只读示例，不证明真实登录态接口。NOT_RUN：微信模拟器与真机视觉验收。本次没有改变平台 API、业务金额、数据库或权限；真实服务写入与后端测试不适用。构建仍有既有 Sass legacy-js-api 提示。

`git pull --ff-only` 返回 Already up to date；定向 stash 恢复后全部任务文件逐字节核对一致；`git diff --check` 退出 0。远端未改变相关代码，构建产物对应最终代码。同步后复跑物品回归和类型检查 PASS。
