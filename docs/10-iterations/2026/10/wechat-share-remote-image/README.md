# 分享欢迎图使用服务器资源

日期：2026-10-03。

## requirement

用户已将既有 share-welcome.png 上传到 haji/wx，要求分享卡片改用服务器图片。

## design

复用 staticResource 集中的公网资源根地址，分享 imageUrl 直接返回 https://hahaen.xyz/minio-api/haji/wx/share-welcome.png。保留20个页面显式注册及固定标题、首页路径。移除本地复制与wx.env依赖；原图移到 offloaded-static-assets/wx 作为备份，渲染脚本同步修改输出位置，防止重新打进代码包。网络图片不可达时不能保证微信客户端加载，需公开可读且服务可用。

## database

无变更，不适用。

## api

无业务接口变更。只读取用户已上传的公开固定图片。

## backend

无变更；Java、权限、金额、事务、数据库、Redis、MinIO业务写入验证不适用。未修改Bucket权限或服务器配置。

## frontend

统一分享工具已改用 staticResource，删除本地复制及wx.env声明；原图迁移到 offloaded-static-assets/wx，渲染脚本同步输出位置。源码回归与生产包检查直接验证真实资源工具返回的HTTPS地址，并检查代码包不含欢迎PNG。20个页面接入方式和标题/路径不变。

## testing

已验证远端HTTP200、image/png、17580字节、500×400，逐字节与本地图相同。分享23项、完整前端153项回归通过；类型及双端构建通过，实际20页生产包回调检查通过且不含欢迎图。微信工具新构建首页分享框实际正常显示标题和远端欢迎图，随后取消；Errors=0，Warnings=5。真机新包验证为补充待验。

## commands

开始时工作区干净，main跟踪origin/main，HEAD为c44fe36。curl公开图片返回HTTP200；Python读取PNG头及对比内容通过。命令未使用凭证。

实际执行：

- `node --test app/tests/wechat-share.test.mjs`：23/23 PASS。
- `node --test app/tests/*.test.mjs`：153/153 PASS。
- `pnpm --dir app run typecheck`：PASS，退出0。
- `pnpm --dir app run build:mp-weixin` / `build:h5`：PASS，退出0，仅既有Sass提示。
- `node app/tests/wechat-share-build.check.mjs`：20/20 PASS，所有页面返回服务器地址，打包不含PNG。
- `node --check app/scripts/render-wechat-welcome.cjs`：PASS；未重新渲染或改变图内容。
- 微信工具：新包首页分享标题与文案图显示正确，取消；未发送消息。
- `git diff --check`：PASS。

## verification

PASS：公开图片HTTP与内容一致性、23项分享/153项完整前端回归、类型、双端构建、20页生产包检查和工具首页实际分享预览。PARTIAL：工具仅手动抽查首页。NOT_RUN（补充）：手机体验版新包图片加载及好友接收打开；未上传/发布。无后端或数据库变更。

## 同步与最终复验

安全暂存13个任务文件（含原路径删除标记）后执行git pull --ff-only，返回Already up to date。恢复后所有SHA-256及删除状态一致，再删除备份。同步后153项回归、类型、20页生产包检查、H5排除与新增文档链接检查、差异检查再次PASS；远端与源码依赖未变化，双端最终构建和工具预览继续适用。

## rollback

恢复上一版分享工具、资源位置和渲染脚本。无需数据库回滚；本轮不删除服务器对象、不上传发布。
