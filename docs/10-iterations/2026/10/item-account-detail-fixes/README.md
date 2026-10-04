# 物品与账户详情修复

## requirement
2026-10-04：修复退役物品详情图表穿过底部按钮；修复资金/信贷账户名称、余额、额度、欠款编辑；账户流水应可查看全部历史。

## design
保持原卡片和账户类型，图表与固定按钮使用普通视图层；详情底部预留100px和安全区。流水按50笔加载，显示总数，滚动触底或按钮续页，失败可重试；筛选重置分页和滚动容器，旧响应丢弃。低于500px高的横屏账户详情允许页面滚动，流水保留260px高度。

## database
不变更Schema、Flyway、Entity或数据。数据库核对不适用。

## api
复用PUT /api/app/accounts/{id}和GET /api/app/accounts/{id}/transactions?type=&page=&pageSize=50。账户流水不传月份/日期；后端现有查询使用当前用户和账户归属，并包含收支、转账两端及还款。无响应契约变更。

## backend
无后端改动；后端测试、打包及真实数据库事务测试不适用于本次前端修改。权限、金额计算、余额事务由原接口承担，本轮不宣称重新验证这些能力。

## frontend
- 编辑回填原使用formatYuan，千元金额含逗号，与整数分解析器冲突；改inputYuan，保留精确分、负余额/负欠款，额度仍非负。打开编辑时重新回填，取消不残留草稿。保存后更新共享账户缓存，刷新详情；资产页回到前台重新加载。
- 编辑弹层增加@click.stop防止点击传播，名称20字校验，保留保存禁用、错误提示与重试。
- 流水原只请求默认第一页50笔；新增分页、总数、加载/错误/已全部显示状态，响应序列隔离和重复ID去重。
- 图表由微信独立canvas层改为普通view线段/刻度/端点，保留既有非等距刻度和负成本算法；固定栏白色背景，底部安全区留白。

## testing
- 169/169前端回归通过，新增金额回填/负值边界、分页接线、图表普通视图与异步筛选/失败重试用例。
- 4个视口（320×568、375×667、414×896、667×375）的生产H5夹具验证，共12个场景通过：两类账户名称/余额/额度/负欠款保存请求、123笔跨年流水、支出筛选62笔、失败重试、退役图表、底部留白、编辑与重新服役弹层。
- 微信产物检查：图表无canvas节点，有普通view线段；账户分页包含bindscrolltolower、编辑弹层包含catchtap。产物检查不替代微信真机。
- H5 375px截图已查看：[退役物品](evidence/retired-item.png)、[资金账户](evidence/account-fund.png)、[信贷账户](evidence/account-credit.png)。夹具为合成数据，不连接真实账户。

## commands
1. node --test app/tests/account-detail-fixes.test.mjs：修复前三个检查失败，定位带分组符回填、分页缺失、canvas路径；修复后4/4通过。静态路径检查不是微信真机复现，截图为用户提供的现象证据。
2. node --test app/tests/*.test.mjs：最终169通过、0失败/跳过。
3. pnpm --dir app run typecheck：退出码0。
4. pnpm --dir app run build:h5 && pnpm --dir app run build:mp-weixin：双端DONE；已有Sass legacy-js-api弃用警告。
5. node app/tests/visual-server.mjs：启动18761独立夹具服务。
6. PLAYWRIGHT_MODULE_PATH=<本机bundled playwright目录> CHROME_EXECUTABLE_PATH=<本机Chrome可执行文件> node app/tests/account-item-detail-browser.cjs：最终12个场景PASS。早期运行先修正验收脚本选择真实内层滚动节点/等待滚动节流，再修复筛选滚动位置和横屏操作栏遮挡，最终全部通过。
7. Python读取微信wxml断言无canvas、存在item-cost-segment、bindscrolltolower、catchtap：PASS。
8. git diff --check及本轮文档链接检查：退出码0。
9. git stash push --include-untracked → git pull --ff-only：Already up to date；apply恢复后15个文件SHA256与备份一致，再drop。同步后前端169项回归与类型检查再次通过；远端未改变代码/依赖，已有双端构建结果仍对应最终源码。

## verification
PASS：前端回归、类型检查、双端构建、生产H5页面夹具运行和微信产物检查。
PARTIAL：运行验证使用H5模拟接口，不能替代真实DEV/生产接口和微信原生平台操作。
NOT_RUN（补充）：微信开发者工具/真机、真实账号编辑与历史流水验收、部署/发布。此次无需微信凭证来运行前端夹具；补充项不列为PASS。
FAIL/BLOCKED：最终本轮必需检查无失败/阻塞；原缺陷和早期失败见commands。

## rollback
回退本迭代代码与文档提交；无Schema和数据迁移，不修补历史账务数据。
