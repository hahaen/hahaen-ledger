# 我的页面资料刷新闪烁修复（2026-10-01）

- requirement：每次切回“我的”时头像和“资料已设置 · 数据随时可用”闪烁。
- design：首次加载保留反馈，同会话再次进入时保留已显示内容并后台刷新；头像未变化且预览地址有效时复用地址。
- database / api / backend：无修改；继续使用当前用户资料和短时头像预览接口。
- frontend：检查 onShow、加载提示、头像回退和短时地址更新；处理会话变化与请求竞态。
- testing：实际 Vue 页面脚本与模板渲染回归，覆盖慢请求、失败、头像更新/移除、预览过期和会话切换。
- commands：见 [执行记录](08-commands.md)。
- verification：必需验证 PASS；实际页面脚本与模板覆盖为 PASS，真实客户端验收 PARTIAL；微信工具/真机和真实用户页面切换 NOT_RUN（补充）。见 [验收记录](09-verification.md)。
- rollback：回退本次 mine.vue、测试及文档提交；无数据库操作。
