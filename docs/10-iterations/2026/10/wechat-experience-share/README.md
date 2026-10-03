# 体验版分享回调注册整改

日期：2026-10-03。

## requirement

用户手机体验版分享仍显示默认小程序名称及首页财务截图，工具显示固定标题和欢迎图。用户确认已上传、更新体验版并完全关闭后重新打开。消除全局 mixin 分享注册对应用初始化顺序的依赖。

## design

检查当前工具项目为生产目录，生产包包含固定标题和 PNG；不能据此证明手机运行的包与本地相同。实际 uni-app 运行时使用 once 缓存全局 mixin 回调：模拟页面先注册、App 后就绪时，首次及后续页面的原生分享方法均缺失；App 先就绪时方法存在。这是已复现的代码缺陷，尚未获得手机调用栈，不能认定它就是此次体验版的唯一根因。改为各页面显式声明分享选项，直接由原生页面初始化读取。图片在分享回调内同步准备，不依赖全局 onLoad。

## database

无变更，不适用。

## api

无变更，不适用。

## backend

无变更；Java、数据库、权限、金额、事务、Redis、MinIO 验证不适用。

## frontend

20 个路由页面在普通 script 的选项中显式声明 onShareAppMessage；移除 main.ts 全局分享 mixin。保留统一标题、首页路径、固定图和复制失败回退；H5 条件编译排除微信选项。首次分享同步准备图片，成功后不重复复制。

## testing

先建立页面注册先于 App 的原生生命周期回归，修复前24项中21项失败（20页面原生方法缺失、首次分享未准备图片），修复后24/24通过。完整前端154/154通过。生产包检查执行实际页面 JS，捕获交给微信的选项并执行 uni-app 原生初始化函数，20/20在 getApp 不可用时仍注册方法并返回固定内容；业务 setup 和微信文件API在该检查中为夹具，不能代替真机证据。微信工具实际首页分享框显示固定标题与欢迎文案图，未使用财务截图，取消后退出，无消息发送。

## commands

已执行：git status --short（干净），git branch -vv（main 跟踪 origin/main）；node --test app/tests/wechat-share.test.mjs（原有4项通过，但仅模拟 App 先就绪）；运行实际 uni-app 生命周期片段与真实 once 语义，App 先就绪得到 function，App 后就绪得到 undefined，后续页面仍 undefined。工具只读检查当前项目路径为 app/dist/build/mp-weixin，构建文件含固定标题与欢迎图。

补充实际命令：

- `node --test app/tests/wechat-share.test.mjs`：修复前21/24 FAIL，修复后24/24 PASS。
- `node --test app/tests/*.test.mjs`：154/154 PASS。
- `pnpm --dir app run typecheck`：PASS，退出0。
- `pnpm --dir app run build:mp-weixin` / `build:h5`：均PASS，退出0，仅既有Sass弃用提示。
- `node app/tests/wechat-share-build.check.mjs`：20/20 PASS，打包PNG与源码逐字节相同。
- H5产物检查：未包含完整微信分享标题或分享工具；框架生命周期常量和既有帮助文案不作为微信接入命中。
- 工具实际新包首页分享预览：标题及图片正确，取消；Errors=0，Warnings=5（既有域名校验配置、系统API弃用、组件选择器及工具脚本预加载）。UI自动化曾因应用bundle定位返回noWindowsAvailable，改用完整应用路径后操作成功。
- `git diff --check`：PASS。

## verification

PASS：24项分享回归、154项全量前端回归、类型检查、双端生产构建、20页实际编译选项的原生生命周期验证、图片一致性与工具首页实际分享卡片。PARTIAL：工具仅手动抽查首页；NOT_RUN（补充）：手机体验版新包复测、好友接收与打开。未上传、设置体验版或发布；原手机现象的唯一根因尚未取得调用栈证明。本轮必需代码回归、双端构建及工具运行通过，真机效果保持待验。

## 同步与最终复验

安全暂存本轮30个任务文件后 `git pull --ff-only` 返回 Already up to date；`git stash apply` 后全部SHA-256一致，确认恢复成功后删除该备份。同步后完整154项回归、类型检查、20页生产包分享检查与差异检查再次PASS。源码、依赖和远端未变化，最终双端构建及工具预览证据继续适用。

## rollback

恢复原 main.ts 全局 mixin、分享工具及页面注册方式，无用户数据或数据库回滚。未上传或发布。
