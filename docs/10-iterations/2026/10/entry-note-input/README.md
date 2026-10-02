# 微信记账备注输入修复

## requirement
新增与编辑记账备注弹窗点击输入框后不能输入并消失；两入口共用 entry 页。

## design
沿用备注弹窗样式、100字限制和草稿确认/取消流程。将背景关闭与触摸拦截移到独立遮罩，内容定位于遮罩上方，移除输入框祖先关闭监听。

## database / api / backend
无变更；金额、归属、事务、幂等与保存接口保持既有实现，Java/数据库验证不适用。

## frontend
只调整 entry 备注弹窗事件边界及遮罩层级。

## testing
模板事件回归先失败，修复后复跑；页面草稿打开、确认、取消回归；类型与双端构建。

## commands
已执行 `node --test app/tests/entry-note-modal.test.mjs`：修复前1项失败，检出输入框祖先关闭/触摸监听。
- `node --test app/tests/entry-note-modal.test.mjs`：修复后2/2 PASS。
- `node --test app/tests/*.test.mjs`：134/134 PASS。
- `pnpm --dir app run typecheck`：退出0。
- `pnpm --dir app run build:h5`、`pnpm --dir app run build:mp-weixin`：退出0，Sass legacy API提示不影响构建。
- `pnpm --dir app run dev:mp-weixin`：Build complete，用于本机工具验证。
- Python检查生产/DEV WXML事件边界与WXSS层级：均PASS。

## verification
PASS：134项前端回归、TypeScript、H5/微信生产构建及微信DEV编译；生产/DEV WXML备注内容祖先无 bindtap/catchtouchmove，关闭与触摸拦截只在兄弟遮罩；内容 position:relative 保证位于遮罩上方。

PASS（微信工具AX操作）：新增记账打开备注，点击输入并填写“备注输入回归”，弹窗保留且6/100；完成回填，重开保留；输入“取消草稿”后取消仍保留原备注。未保存真实账单。

PASS（实际页面函数脚本）：新增空备注/编辑已有备注打开、确认多行文本、取消后重新打开恢复原值及禁用不打开。

PARTIAL：原始用户症状未在工具AX点击中完整复现；模板回归修复前明确检出祖先关闭与拦截。根因判断为微信内部点击冒泡到祖先关闭路径，修复移除该路径；尚无真机事件轨迹。

BLOCKED（补充）：微信原生坐标输入报 noWindowsAvailable；H5浏览器访问因用户拒绝权限被工具阻止，未绕过。

NOT_RUN（补充）：真实编辑账单UI、微信手机输入键盘、H5浏览器手动验收、真实保存及发布。必需代码回归、类型、双端构建和微信共享弹窗运行验证均已完成；补充验收不冒充PASS。

## sync
本次文件按路径stash备份后执行 `git pull --ff-only`：Already up to date。apply后逐文件与备份比较一致，再删除备份。同步后备注/触摸/滚动10项定向回归PASS；复跑类型和双端构建。工作区另一项NativeNavigation及其迭代/测试不属于本次提交。

## rollback
恢复 entry 备注弹窗祖先监听和对应样式；不涉及数据回退。
