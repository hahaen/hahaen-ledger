# 前端

## 实际修改

- `app/src/prototype.scss`
  - 认证页改为允许纵向滚动并补充底部安全区。
  - 文本按钮、返回按钮、日历控制、筛选按钮、计算提示和主要操作按钮显式居中。
  - 通用居中弹层、退款/还款/月份/记账选择弹层补充安全区、滚动边界和动态视口高度。
  - 未改变业务脚本、接口调用、固定层级和整体配色。
- `app/src/styles.scss`
  - 帮助/协议滚动内容补充底部安全区和惯性滚动。
  - 帮助返回按钮与个人中心动态视口规则统一。
  - 个人中心保存按钮和修改密码确认按钮改用明确的 `#49ad9c` 背景色，并关闭微信原生 `button::after`，修复小程序显示为空白按钮。
- `app/src/pages/profile/profile.vue`
  - H5 保留动态原生文件输入；微信小程序改为 `uni.chooseImage`，选中后调用工具层上传并立即预览，保存时仍只提交 `avatarFileId`。
  - 未加入 `wx.getUserProfile`、`wx.getUserInfo` 或其他微信头像/资料获取逻辑。
- `app/src/utils/file.ts`
  - 增加小程序临时文件的 SHA-256、图片头识别、二进制读取和预签名 PUT 上传，复用既有上传申请、完成和预览接口。
  - 使用 `getFileSystemManager().getFileInfo` 替代已废弃的 `uni.getFileInfo`，只读取实际文件大小；因小程序文件信息接口不保证 SHA-256，改由同一份二进制内容计算 SHA-256 后再上传。
  - 预签名 PUT 显式设置 `dataType: 'text'`，并将合法域名、超时、HTTPS 和 HTTP 状态错误转换为可读提示。
- `app/src/pages/entry/entry.vue`
  - 将新增/编辑记账页的 `screen-nav` 移到 `.entry-content` 外，避免导航与主体滚动层及微信小程序胶囊区域混用；脚本、按钮文案和事件未变。
- `app/src/prototype.scss`
  - 为 `.entry-page` 增加 flex 页面结构和直接子级导航约束；在 `MP-WEIXIN` 下预留 44px 自定义导航栏空间，并锁定 `.entry-type button` 的 38px 高度、零内边距和单倍行高。
- `app/tests/responsive-layout.test.mjs`
  - 新增认证页纵向滚动、按钮居中、个人中心按钮外观、记账页小程序顶部控件、弹层安全区/滚动和核心 scroll-view 结构回归。
- `app/tests/page-flows.test.mjs`
  - 增加小程序临时文件上传和“仅用户选择、不获取微信头像资料”的回归。

## 范围核对

本轮修改仍未改变后端、数据库和 API 协议；前端范围包含上述共享样式、个人中心头像页面、文件上传工具和对应回归测试。
