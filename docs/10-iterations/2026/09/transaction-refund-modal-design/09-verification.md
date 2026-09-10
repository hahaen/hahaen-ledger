# 验证

| 范围 | 状态 | 证据 |
| --- | --- | --- |
| 退款弹层结构与样式 | PASS（静态核对） | `detail.vue` 使用 `refund-*` 专用弹层；样式复用系统 30px 圆角、薄荷绿令牌、大按钮和安全区规则 |
| 默认回填剩余可退款金额 | PASS（静态核对） | `openRefund()` 使用 `inputYuan(detail.value.effectiveCents)`，确认逻辑仍使用既有整数分转换 |
| 退款后主金额 | PASS（静态核对） | 详情主金额改为 `effectiveCents`，移除重复的“当前有效金额”信息行 |
| 退款记录区样式 | PASS（静态核对） | 增加 `REFUND RECORD` 眉标题，使用暖色汇总卡片和退款明细行 |
| 退款 API、幂等和错误重试 | PASS（范围核对） | 未修改 `refund()` 的既有接口、幂等键和错误提示流程 |
| 退款删除后的前端状态 | PASS | `confirmDelete()` 成功后立即移除退款、回算有效金额并始终释放 `saving`；详情刷新失败不回显旧记录 |
| 删除失败反馈 | PASS（静态核对） | 后端业务错误会显示在确认弹窗内；失败不会关闭弹窗或修改当前详情 |
| 退款删除后的后端事务 | PASS（单元测试） | `deleteRefund()` 软删除退款、回算账单金额并恢复支出账户余额 |
| 前端回归测试 | PASS | Node tests 18/18，包含详情首次生命周期回归 |
| 后端回归测试 | PASS | Maven tests 26/26 |
| TypeScript 类型检查 | PASS | `pnpm run typecheck` exit 0 |
| H5 生产构建 | PASS | `pnpm run build:h5` 完成 |
| 微信小程序生产构建 | PASS | `pnpm run build:mp-weixin` 完成 |
| 本机视觉夹具截图 | BLOCKED | Codex 内置浏览器打开 `127.0.0.1:18761` 返回 `ERR_BLOCKED_BY_CLIENT` |
| 真实登录态/退款写入验收 | NOT_RUN | 本次未取得有效登录态；视觉夹具为只读数据且不执行写入 |
