# 验证

本轮完成后按以下范围记录证据：

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 静态布局回归 | PASS | `tests/*.test.mjs` 共 53/53；覆盖认证滚动、按钮居中、个人中心小程序按钮外观和头像上传、新增记账顶部控件、弹层安全区/滚动及核心 scroll-view 结构 |
| 前端现有回归 | PASS | 同一命令包含既有页面/认证/交互回归，全部通过 |
| 类型检查 | PASS | `pnpm run typecheck`，exit 0 |
| Lint | BLOCKED | `pnpm run lint` 失败，项目未定义 lint script/依赖 |
| H5 构建 | PASS | `pnpm run build:h5`，输出 `DONE Build complete.` |
| 微信小程序构建 | PASS | `pnpm run build:mp-weixin`，输出 `DONE Build complete.` |
| 个人中心小程序按钮产物 | PASS（静态） | `profile.wxml` 保留保存按钮节点；`app.wxss` 使用明确 `#49ad9c` 背景色、白色文字并清理原生 `button::after` |
| 个人中心小程序头像上传产物 | PASS（静态） | `profile.js` 使用 `chooseImage` 和 `uploadAvatarFromMiniPath`；产物未包含 `getUserProfile`/`getUserInfo`；工具层使用临时文件摘要、图片头识别和预签名 PUT |
| 小程序文件信息 API 和预签名 PUT 兼容性 | PASS（静态+回归） | 使用 `getFileSystemManager().getFileInfo` 读取大小，由上传的同一二进制内容计算 SHA-256；预签名 PUT 设置 `dataType: 'text'`，产物未包含 `uni.getFileInfo` |
| H5 实际页面/交互 | PARTIAL | 认证/注册/协议页可显示；密码显隐可点击；协议正文可滚动；仅 771×1272 浏览器，未覆盖窄屏设备、真实登录账务和全部真实数据弹层 |
| H5 横向溢出 | PASS（当前浏览器） | `innerWidth=771`、`scrollWidth=771`、`clientWidth=771` |
| 微信开发者工具实际页面/交互 | BLOCKED / NOT_RUN | 已确认 `D:\software\微信web开发者工具\微信开发者工具.exe` 存在，但当前 UI 自动化面未暴露可控制的原生窗口，未导入 `dist/build/mp-weixin` |
| 接口/业务/数据库影响 | PASS（范围核对） | 仅共享 SCSS、记账页布局结构、布局回归测试和迭代文档变化；未修改 API、方法、业务逻辑或数据库 |

## 2026-09-17 追加：新增记账页小程序顶部控件

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 自定义导航结构 | PASS（静态） | `entry.vue` 中 `screen-nav` 位于 `entry-content` 之前，主体滚动不会带走顶部导航。 |
| 微信小程序胶囊避让 | PASS（产物静态） | `app.wxss` 含 `MP-WEIXIN` 编译出的 `.entry-page` `44px` 导航栏预留。 |
| 支出/收入/转账按钮尺寸 | PASS（静态+产物） | `.entry-type button` 显式设置 `38px` 高度、零内边距、`line-height:1`、flex 居中并清理 `::after`。 |
| 前端回归 | PASS | `pnpm exec node --test tests/*.test.mjs`，53/53。 |
| 类型检查与双端构建 | PASS | `pnpm run typecheck`、`pnpm run build:h5`、`pnpm run build:mp-weixin` 均完成。 |
| H5 实际新增记账页 | NOT_RUN | 本次仅完成构建与静态核对，未重新取得真实登录态操作页面。 |
| 微信开发者工具/真机实际画面 | BLOCKED / NOT_RUN | 当前 UI 自动化面未暴露可控制的微信开发者工具原生窗口，未能导入产物并点击验证。 |
