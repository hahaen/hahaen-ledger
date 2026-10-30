# 彩票表单统一提示（2026-10-30）

## requirement

截图中的期号校验错误仅显示在长表单底部，容易被忽略。彩票表单的号码、期号、倍数、期数、日期、名称及购买确认等失败反馈改用项目默认 Toast。

## design

复用 `uni.showToast({ title, icon: 'none' })`，移除保存时的底部红字；不引入新组件、样式或平台分支。加载失败仍保留重试入口；失败保留输入和幂等键，允许修正后重试。

## database / api / backend

均不变，无 Migration、SQL、配置变更。网络与接口错误原本已由 `utils/api.ts` 统一 Toast；本次补齐页面保存异常反馈。

## frontend

仅修改共享彩票编辑页的保存失败反馈，覆盖新建/编辑单次购买、定投计划、登记实际购买。H5 与微信共用 `uni.showToast`。

## testing / verification

PASS：本次差异 Review；彩票回归 5/5（同步前4/4，同步后新增远端既有进度用例）（包含大乐透/双色球错误期号、未确认购买、失败保留输入/幂等键及修正后保存）；TypeScript、H5/微信命令行构建。

PASS：Chrome 本地 H5 只读夹具在 375×667 看到居中“大乐透期号须为5位”，底部错误元素为0；填入期号后提示“请确认已经实际购买”，667×375 横屏提示可见且页面宽度等于视口宽度。夹具禁止写入并限制网络为同源，不连接真实业务后端；不将夹具视为真实业务保存验收。[375px 截图](evidence/h5-toast-375.png)。

微信工具/真机运行与发布为 NOT_RUN（按用户阶段规则排除，非必需）。无未解决必需 FAIL/BLOCKED。

## commands

- `git diff --check`：PASS。
- `cd app && node --test tests/lottery.test.mjs`：PASS，同步前4/4、同步后5/5。
- `cd app && pnpm run typecheck`：PASS，退出0。
- `cd app && pnpm run build:h5`：PASS，退出0。
- `cd app && pnpm run build:mp-weixin`：PASS，退出0；仅项目脚本，未启动微信工具。
- 本地临时只读服务器与 Chrome 操作：PASS，见上述画面证据；临时脚本不提交。

构建仅输出既有 Sass legacy-js-api 弃用提示，无新增构建失败。

## rollback

如需撤销，新增正向提交恢复编辑页反馈逻辑并重新发布 H5；无数据库回退，不创建旧应用备份、不自动回滚。

## Review

2026-10-30：仅审查本次编辑页两处差异和 Toast 回归；确认校验失败不触发写请求，输入、幂等键、保存中禁用、加载失败重试和平台共用调用不变。接口层与页面重复提示时由原生 Toast 替换当前消息，不新增页面弹层。未改权限、金额计算或事务。结论 PASS，无未解决问题，随后开始验证。

## upstream 同步与复审

安全保存本轮改动后执行 `git pull --ff-only`，从 37c04d2 快进到 6a06264；远端仅增加定投进度及其验收记录。`git stash apply` 恢复成功，前端/设计规范追加段落冲突按语义保留双方有效内容，彩票测试自动合并。对本轮编辑页、测试与文档恢复差异复审 PASS，无未解决问题；接口与编辑页依赖签名未改变。同步后彩票回归5/5、TypeScript和H5/微信命令行构建均退出0；原编辑页代码和截图完整恢复，恢复确认后删除stash备份。
