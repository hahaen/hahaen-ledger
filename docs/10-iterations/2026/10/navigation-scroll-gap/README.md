# 滚动后导航下方空隙修复（2026-10-03）

- requirement：除首页外各页面滚动后顶部出现大空隙；所有共用导航页面与首页一致，微信胶囊下沿到初始正文保持10px。
- design：页面padding与导航占位margin使用同一CSS安全区表达式 `max(var(--status-bar-height, 25px), env(safe-area-inset-top, 0px))`，再抵消调用方原有0/20/60px留白；固定层仍使用胶囊视口坐标。移除mounted/nextTick/SelectorQuery一次性坐标补偿，避免异步布局或采样时滚动影响正文位置。H5无胶囊时继续原布局。
- database/api/backend：无变更，不适用；无真实账务写入，金额、事务、幂等、权限、Redis/MinIO无需新增验证。
- frontend：仅修改NativeNavigation；所有品牌页头、记账、账单/账户/物品详情、资料、通知、待办、帮助、协议、首次使用页共享修复，不逐页调整业务。
- testing：真实SFC在Chrome编译运行，四视口（320×568、375×667、430×932、667×375）×两种顶部CSS高度×13种真实调用配置=104组PASS；覆盖首段10px、安全区25↔54px变化、滚到300/600/0、导航固定、返回及无横向溢出。安全区异步变化为可重复复现条件，不等同于已读取手机内部渲染时序。
- evidence：[组件几何](evidence/component-scroll.jsonl)、[H5日历](evidence/h5-calendar.jsonl)、[H5记账](evidence/h5-entry.jsonl)、[H5滚动截图](evidence/h5-calendar-scrolled.png)。微信iPhone 5模拟器实际日历长内容上滑，月历和汇总移动，页头和底栏固定；滚回顶部恢复初始间距。见[首屏](evidence/wechat-calendar-top.png)、[滚动](evidence/wechat-calendar-scrolled.png)、[回顶](evidence/wechat-calendar-return-top.png)。
- commands：开始时git status --short无输出，main跟踪origin/main，暂存区为空。修复前 `node app/tests/fixed-navigation-browser.cjs` FAIL：正文Y=131，预期102（多出29px）；修复后104/104 PASS。`pnpm --dir app run typecheck` PASS；`node --test app/tests/*.test.mjs` 134/134 PASS；`pnpm --dir app run build:h5`、`pnpm --dir app run build:mp-weixin` PASS。本机Playwright/Chrome执行 `calendar-scroll-browser.cjs` 4视口PASS，`entry-scroll-browser.cjs` 4视口×新增及三种编辑=16组PASS。微信产物检查PASS：使用CSS抵消，无createSelectorQuery补偿。只读H5服务18761为本轮启动，结束时停止；9898/5180原有服务保留。
- verification：PASS为上述回归、类型、构建、H5触摸及微信模拟器已执行场景。PARTIAL：未在微信工具逐页覆盖全部13种配置，全部调用配置已有104组组件几何证据。手机微信/iOS Safari及发布NOT_RUN（补充）；后端/数据库不适用。本轮CLI因服务端口关闭不可用，未修改安全设置；原生窗口控制曾短暂noWindowsAvailable，重绑应用、刷新可访问性状态后取得实际滚动截图，日历、物品、记账工具验证已恢复。
- sync：包含未跟踪文件的stash备份后 `git pull --ff-only` → Already up to date；恢复后23个文件SHA-256及文件范围全部一致，无未合并文件，确认成功后删除stash。同步后再次执行类型检查、134项回归、104组组件几何及H5/微信构建，全部PASS；代码与依赖没有远端变化。
- 微信补充（PASS）：物品页原生页面滚动与记账主体滚动均已执行，上滑时导航/底栏/键盘固定，回顶恢复10px间距。分别保存物品[首屏](evidence/wechat-items-top.png)/[滚动](evidence/wechat-items-scrolled.png)/[回顶](evidence/wechat-items-return-top.png)，记账[首屏](evidence/wechat-entry-top.png)/[滚动](evidence/wechat-entry-scrolled.png)/[回顶](evidence/wechat-entry-return-top.png)；未点击保存，机型已恢复为原iPhone 12/13 (Pro)。
- rollback：恢复本轮共享导航及相关测试，无数据库回退。
