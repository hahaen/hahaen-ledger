# 微信顶栏初始间距优化（2026-10-03）

- requirement：初次打开页面时顶部与正文距离过大，缩小间距并保持导航固定。
- design：按用户最终要求，全部微信顶栏与正文间距统一10px，不做账户详情特例；品牌页头补偿.page已有20px顶部留白，物品详情同样补齐。保持胶囊定位、H5布局与底部空间。首屏查询真实占位坐标，校正CSS与系统安全区高度不一致带来的偏差。
- database/api/backend：无变更，不适用。
- frontend：统一NativeNavigation间距、PageHeader和物品详情顶部补偿。
- testing：本机Chrome运行实际Vue SFC，参数从8个真实调用方读取，页面padding独立给定；四尺寸×两种安全区实际高度×八调用方共64组，校验10px首段间距、正文滚动、顶栏固定、返回与无横向溢出。见[证据](evidence/component-scroll.jsonl)。
- commands：`pnpm --dir app run typecheck` PASS；`node --test app/tests/*.test.mjs` 134/134 PASS；`pnpm --dir app run build:h5`及`pnpm --dir app run build:mp-weixin` PASS；本机已安装Playwright与Chrome运行`node app/tests/fixed-navigation-browser.cjs` 64/64 PASS。初次平台nextTick类型问题已改用Vue nextTick并修复可空坐标检查，复验通过。
- verification：PASS为上述代码/构建/运行布局，以及本机微信工具iPhone12/13模拟器首页、资产页初始间距抽查，胶囊下沿到卡片顶部约10逻辑像素，首卡圆角完整。PARTIAL：手动未逐页抽查，账户详情已由真实调用参数布局回归覆盖。真机、发布NOT_RUN（补充），本轮无必需FAIL/BLOCKED。只读验收中账户行进入排序选择状态，已点击同一源账户退出，没有交换或保存用户数据。
- rollback：恢复上述组件及物品详情的间距设置，无数据回退。
