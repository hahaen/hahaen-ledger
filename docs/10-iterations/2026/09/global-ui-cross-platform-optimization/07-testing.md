# 测试

实际执行：

- `pnpm exec node --test tests/*.test.mjs`：53/53 PASS（包含个人中心小程序按钮外观、头像上传和新增记账顶部控件回归）。
- `pnpm run typecheck`：PASS，exit 0。
- `pnpm run lint`：BLOCKED，项目无 `lint` script。
- `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5`：PASS，输出 `DONE Build complete.`。
- `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:mp-weixin`：PASS，输出 `DONE Build complete.`。
- `git diff --check`：PASS。
- 微信小程序产物静态核对：`app/dist/build/mp-weixin/pages/profile/profile.wxml` 保留 `profile-save-action` 按钮节点，`app/dist/build/mp-weixin/app.wxss` 含 `#49ad9c` 背景色及 `:after { display:none; border:0; }`。
- 微信小程序头像产物静态核对：`profile.js` 含 `chooseImage` 和 `uploadAvatarFromMiniPath`，不含 `getUserProfile`/`getUserInfo`；`utils/file.js` 使用 `getFileSystemManager().getFileInfo` 读取大小、对同一二进制内容计算 SHA-256、使用 `dataType: 'text'` 和 PUT 上传，未残留 `uni.getFileInfo`。
- H5 实际浏览器：认证/注册页显示，密码显隐可点击，协议正文长内容可滚动；浏览器视口 771×1272、横向溢出指标为 0。
- 微信开发者工具：BLOCKED/NOT_RUN；已确认安装文件 `D:\software\微信web开发者工具\微信开发者工具.exe` 存在，但当前 UI 自动化面未暴露可控制的原生窗口。

本次追加产物核对：`app/dist/build/mp-weixin/pages/entry/entry.wxml` 中 `screen-nav` 位于 `entry-content` 之前；`app/dist/build/mp-weixin/app.wxss` 含 `.entry-page` 的 44px MP 导航预留和 `.entry-type button` 的显式尺寸/行高。该核对仍不等同于真实微信页面验收。
