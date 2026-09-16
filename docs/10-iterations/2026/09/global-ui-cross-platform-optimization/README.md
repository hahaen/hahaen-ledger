# 全局 UI 与 H5/微信小程序跨端适配优化

本轮目标是在不改变现有接口、业务方法、数据结构和视觉方向的前提下，系统检查前端页面、组件、共享 SCSS 和平台边界，修复小屏显示、滚动、按钮对齐、弹层安全区和 H5/微信小程序布局差异。

当前范围仅包含 `app/` 前端样式、布局及必要的跨端 UI 兼容；后端、数据库、接口协议和业务逻辑不变。

状态遵循 `PASS / PARTIAL / FAIL / BLOCKED / NOT_RUN`，构建通过不等同于真实微信开发者工具验收通过。

## 本轮结果

- `PASS`：共享样式静态布局回归 53/53、既有前端回归、类型检查、H5 构建、微信小程序构建和空白检查。
- `PASS`：个人中心底部保存/修改密码按钮补充微信原生 `button` 的明确背景色、文字色和 `::after` 清理，避免小程序中出现白色空按钮。
- `PASS`：微信小程序个人中心头像改为用户通过相册/相机自行选择并上传，沿用 H5 的预览、保存后关联流程，不调用微信头像资料接口。
- `PASS`：修复小程序头像上传兼容性：文件信息改用 `getFileSystemManager().getFileInfo`，预签名 PUT 使用文本响应类型，避免微信废弃 API 和空响应解析造成失败提示。
- `PASS`：H5 浏览器认证/注册/协议页面显示，密码显隐点击和协议长内容滚动。
- `PASS`：新增记账页顶部导航移出滚动内容；微信小程序专用样式为自定义导航预留胶囊区域，并显式约束支出/收入/转账按钮的高度、行高、内边距和伪元素。
- `PARTIAL`：H5 尚未在窄屏设备模拟、真实登录账务页面和所有真实数据弹层上完成运行时验收。
- `BLOCKED / NOT_RUN`：本机安装文件存在，但当前 UI 自动化面未暴露可控制的原生微信开发者工具窗口，未进行真实小程序页面、safe-area 和交互验收。

## 目录

- [01-requirement.md](01-requirement.md)
- [02-design.md](02-design.md)
- [03-database.md](03-database.md)
- [04-api.md](04-api.md)
- [05-backend.md](05-backend.md)
- [06-frontend.md](06-frontend.md)
- [07-testing.md](07-testing.md)
- [08-commands.md](08-commands.md)
- [09-verification.md](09-verification.md)
- [10-rollback.md](10-rollback.md)
