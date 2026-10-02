# 固定启动页分享文案图

日期：2026-10-02，用户确认后接入。

## requirement

全局好友/群分享卡片中间仅显示用户确认的启动页文字：“记录每一笔，让生活更清晰”“用 3 秒记下一笔，用 10 秒看懂这个月。”；不含图标、叶子插画、账单或余额。分享标题与首页落点保持原配置。

## design

本地500×400 PNG，5:4比例，约17KB。米白背景、深色首行、绿色第二行、灰色说明。固定 imageUrl 指向代码包内资源，避免依赖远程图加载。不读取或截取当前用户页面。图片已在对话中预览并得到“确定”授权。

## database

无变更，不适用。

## api

无变更，不适用。

## backend

无变更；Java、金额事务、权限和数据验证不适用。

## frontend

更新全局分享固定图来源为 `/static/share-welcome.png`；页面首次加载复制到微信USER_DATA_PATH后分享，失败仍指定固定图，不使用当前页面截图。保留 HTML 原稿与渲染脚本，可使用 Playwright/Chrome重新输出固定文案图；产物直接入库，普通构建无需运行渲染脚本或新增依赖。

## testing

分享原生运行时返回固定本地文件路径；本地复制失败也保留固定图。PNG格式500×400且小于200KB；微信工具实际首页分享框已显示确认文案图，不含图标、插画、账单或余额。仅打开确认框后取消，无消息发送。

## commands

初始工作区仅有本任务上轮待确认的三个未跟踪文件（HTML原稿、渲染脚本、PNG）。上轮已使用本机Playwright与Chrome执行 `node app/scripts/render-wechat-welcome.cjs`，PASS：500×400、无图标、仅确认文案、无账单余额与溢出。

重新渲染时设置 PLAYWRIGHT_MODULE_PATH 为可用Playwright路径、CHROME_EXECUTABLE_PATH 为Chrome可执行文件；不包含用户数据。确认后的PNG直接用于分享，避免不同机器字体重新渲染导致外观变化。

- node --test app/tests/*.test.mjs：最终132/132 PASS。
- pnpm --dir app run typecheck：PASS，退出0。
- pnpm --dir app run build:h5 / build:mp-weixin：最终均PASS，退出0；既有Sass弃用提示。
- 构建PNG内容比对：确认图与微信打包图SHA-256一致。
- 微信工具：运行最终构建，固定文案图加载并显示、无账单余额；取消预览，没有发送。

## verification

PASS：确认图与微信打包图片内容一致，微信工具首页分享框图片加载并显示正确；工具Errors=0，五项告警为既有平台提示（域名校验配置、系统信息API弃用、组件选择器及工具脚本预加载）。PARTIAL：模拟器抽查首页；NOT_RUN（补充）：真机转发与好友接收，上传/发布。

微信工具问题处理：代码包 `/static/...` 与 `static/...` 路径在工具分享框均显示破图，getImageInfo实测可读取PNG（500×400）。改为页面加载时复制固定图到微信USER_DATA_PATH，分享框实际显示通过；未读取或截取用户页面。初次类型检查发现uni.env无声明，改用工具层明确类型的wx.env；一次测试路径替换误删src/static分隔符已修正。所有失败均修复后复验。

同步：git stash push --include-untracked → git pull --ff-only（Already up to date）→ git stash apply；11个任务文件哈希一致后删除备份。同步后132项测试及类型检查PASS，git diff --check通过。源码与依赖未变化，最终双端构建及微信工具运行证据仍适用。

## rollback

恢复上一版分享 imageUrl 和测试/规范，删除本轮固定图与原稿/脚本。无数据库或用户数据回滚。
