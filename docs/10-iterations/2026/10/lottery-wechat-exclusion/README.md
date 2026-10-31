# 微信小程序剥离彩票（2026-10-31）

## requirement

用户要求微信小程序不出现彩票功能；H5 不动。

## design

通过 uni-app 条件编译从微信构建中移除四个彩票路由、我的入口与导航函数、彩票 API 和全局彩票样式。不以账号权限或运行时隐藏代替构建剥离；保留 H5 原有实现和权限判断。

## database / api / backend

无数据库、Migration、SQL、配置或后端修改；后端 API 契约和数据保持原状。

## frontend

仅排除 MP-WEIXIN，其他平台沿用现有行为。页面源文件保留供 H5 使用；现有五项导航、待办和通知中心保持可用。现有五处配置解析测试改用真实 uni-app 预处理，微信分享检查只遍历实际注册的21页。新增6项平台/真实模板渲染回归与全量产物扫描脚本。

## testing

PASS：首轮 Review 覆盖四个源文件及本次测试/扫描脚本，条件编译边界完整，不改权限/金额/事务/幂等/后端数据。初次定向测试中测试夹具未分开预处理 SFC 脚本且未成功注入资料加载，导致2项断言失败；修正测试夹具后复审并重跑6/6通过，无业务代码补改或未解决问题。

PASS：前端回归206/206（原204减去微信已剥离4页分享测试，再增加6项平台测试），TypeScript、H5/微信命令行构建。构建保留已有 Sass legacy-js-api 警告，无构建错误。

PASS：微信 app.json 仅21页，扫描140个产物文件，文件名及 JS/JSON/WXML/WXSS/map 内容无 lottery/彩票/双色球/大乐透；四个页面、彩票组件/工具/API/样式均未打包。H5 构建保留全部四个路由、入口文案、API 和样式。权限 true/false 的实际模板渲染覆盖 H5 显示/隐藏、微信始终无入口，待办和通知中心仍显示。

PASS：使用真实 uni-app 预处理对比修改前 HEAD 与修改后四个源文件，H5 结果忽略空行后逐字相同；H5 彩票页面/业务辅助/样式内容均未修改。无需额外生产业务写入或数据库集成验证；后端测试不适用。

## commands

- `git status --short`、`git branch -vv`：开始时工作区/暂存区为空；main 配置 origin/main，起点27bfbd0。
- `git diff --check`：PASS。
- `cd app && node --test tests/lottery-platform.test.mjs`：首次4/6、修正夹具后6/6。
- `cd app && node --test tests/*.test.mjs`：206/206，无失败或跳过（输出保存在本机/tmp）。
- `cd app && pnpm run typecheck`：退出0。
- `cd app && pnpm run build:mp-weixin`：退出0；仅项目构建，不调用微信工具。
- `cd app && pnpm run build:h5`：退出0。
- app目录执行临时Node脚本，以`git show HEAD:app/src/<文件>`和真实预处理器对比四个修改文件的H5分支（剔除空行）：全部相同。
- `cd app && node scripts/check-lottery-platform.mjs`：PASS，21页/140文件无彩票，H5保留功能。

- `git stash push -u` → `git pull --ff-only` → `git stash apply --index`：Already up to date；20文件恢复SHA256一致后删除本轮stash备份。
- 同步后范围复审：PASS；`node --test tests/*.test.mjs`退出0/206通过；`pnpm run typecheck`及产物扫描再次退出0。源码与构建输入未变，复用已通过的双端构建。

## verification

实现及必需验证 PASS，无未解决必需 FAIL/BLOCKED/PARTIAL。同步后范围复审与复验 PASS：20文件安全保存/恢复SHA256一致，远端无更新、无冲突；前端206/206、类型及产物扫描再次通过。提交推送以本轮Git记录为准。微信工具/真机运行及上传发布 NOT_RUN（按用户阶段规则排除，非必需）；本轮未操作微信开发者工具。

## rollback

需要撤回时新增反向提交并重新验证；不删除后端彩票数据。后端及 H5 无需更新或发布；微信小程序需用户自行上传发布新版。本轮不执行任何服务器部署或微信开发者工具操作。

## 2026-10-31 追加：永久工程规则

requirement/design：按用户要求，将微信禁止包含彩票、H5保留彩票写入根目录[AGENTS.md](../../../../../AGENTS.md#彩票功能的平台永久边界2026-10-31)，约束后续开发。明确账号权限开启也不能恢复微信入口或页面，构建产物必须剥离彩票内容；跨端同步及恢复工具操作均不改变该功能边界。H5保留原有权限和功能，后端数据及所需接口不得因此删除或关闭。

database/api/backend/frontend：本次仅修改工程规则及必要审计记录，业务代码、数据库、接口和配置均未改变。testing：仅做本次文档内容Review、链接和Git空白检查，不重复业务测试或构建。commands/verification：首轮Review确认与现有平台隔离实现及用户要求一致；验证和远端同步结果见后续实际执行记录。rollback：如需调整规则，以用户明确的新要求修改文档；本次无需更新任何服务或客户端，后端/H5/微信小程序均无需发布。

文档验证PASS：本次5个文档内容/链接目标/锚点及脚本路径检查、`git diff --check`通过；首轮链接Review修正了AGENTS相对路径层级，复查通过。安全stash后`git pull --ff-only`为Already up to date，恢复5文件SHA256一致后删除本轮备份；同步后本次范围复审及空白复验通过，无冲突或未解决FAIL/BLOCKED/PARTIAL。纯文档不执行业务构建、微信工具或服务器部署。
