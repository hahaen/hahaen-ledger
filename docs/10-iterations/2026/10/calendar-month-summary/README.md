# 日历月度汇总与当天收支

- requirement：日历卡片改为当月支出、当月收入、当月结余；日期行笔数左侧增加首页同款当天“收 / 支”。当月指日历所选月份。
- design：沿用现有卡片、MoneyDisplay、收入/支出颜色与 date-flow；窄屏和长金额允许换行，笔数保持右侧。
- database/api/backend：无变更，复用月度汇总与单日详情接口，统计口径由后端提供。
- frontend：月度汇总保存在页面局部状态，切月/退出清除，旧请求不能覆盖新月份；单日加载失败时不显示虚假的零收支。
- testing：PASS：165/165前端回归、类型检查、H5/微信生产构建。只读浏览器夹具覆盖320×568、375×667、414×896、667×375：点击日期月度汇总不变、当天收支与笔数顺序正确、空日收0支0、单日失败隐藏收支/月度保留/重试恢复、切月更新、长金额无横向溢出。
- commands：`node --test app/tests/*.test.mjs` → 165/165 PASS；`pnpm --dir app run typecheck`、`pnpm --dir app run build:h5`、`pnpm --dir app run build:mp-weixin` → PASS；`node app/tests/visual-server.mjs` 启动独立只读服务；设置本机PLAYWRIGHT_MODULE_PATH与CHROME_EXECUTABLE_PATH运行 `node app/tests/calendar-summary-browser.cjs` → 4视口PASS。`git diff --check`及同步后复验结果见后续记录。
- verification：必需自动验证PASS；PARTIAL：运行证据为只读H5夹具，不代表真实账务接口验收；NOT_RUN（补充）：微信真机、真实账号数据、部署/发布。未更改后端、Schema、权限、账务写入、Redis/MinIO，无相应新增验证要求。
- evidence：[H5 375px截图](evidence/h5-375.png)，运行断言位于 `app/tests/calendar-summary-browser.cjs`。
- rollback：恢复日历页面和本次局部样式；无数据库回退。

- sync：包含未跟踪文件的stash备份后执行 `git pull --ff-only` → Already up to date；恢复后10个文件SHA-256全部一致，再删除备份。同步后165/165回归、类型检查、H5/微信构建、4视口浏览器运行复验PASS；无FAIL/BLOCKED。
