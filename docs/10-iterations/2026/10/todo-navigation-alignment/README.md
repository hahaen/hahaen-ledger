# 待办页头与通知中心对齐（2026-10-01）

## Requirement / Design

用户真机截图显示待办清单标题比通知中心低20px。待办清单、详情、编辑同为page profile-page，继承公共20px额外顶部留白；统一传入page-top-extra=20，按微信胶囊实时位置补偿。H5无胶囊数据时不应用该参数。

## Database / API / Backend

无变化，不适用数据库及Java验证。

## Frontend

待办三个页面补齐NativeNavigation的page-top-extra参数，与通知中心、个人中心一致。

## Testing / Commands / Verification

- PASS：新增回归读取三个待办页面及通知中心的真实调用参数，执行NativeNavigation实际computed样式。修复前待办top=80而胶囊top=60，稳定复现20px下移；补齐参数后全部top=60，height=32px。无胶囊的H5前后style相同。
- PASS：`cd app && node --test tests/*.test.mjs`，110/110无失败/跳过；`pnpm run typecheck`退出0；`pnpm run build:h5`与`pnpm run build:mp-weixin`均DONE Build complete，既有Sass弃用提示。
- PASS：`git diff --check`退出0。
- PARTIAL：平台定位由实际组件计算与截图根因闭环；本轮工具只显示项目列表，打开项目尝试遇到失效元素，未完成修复后模拟器画面核对。
- NOT_RUN（补充）：修复后微信真机复验，不能用计算回归或构建替代。未做业务写入。

## Rollback

回退本次三个导航参数、回归测试与文档，无迁移或用户数据变化。

## 同步与交付

stash含未跟踪文件保存10文件后，git pull --ff-only返回Already up to date。恢复后10文件SHA256一致，再删除备份；同步后定向2项回归与类型检查再次PASS。最终源码与已通过双端构建的版本一致；无远端代码/依赖更新，不重复构建。逐文件暂存并检查diff，提交推送以Git结果与交付报告为准。
