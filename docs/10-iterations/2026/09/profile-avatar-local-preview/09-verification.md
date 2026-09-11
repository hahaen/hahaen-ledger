# 验证

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 选择后立即本地预览 | PASS（前端流程回归） | `page-flows.test.mjs` 断言上传成功后页面立即采用返回的 `viewUrl`。 |
| 保存前不更新资料 | PASS（前端流程回归） | 选择阶段资料请求列表为空，未调用 `PUT /api/app/user/profile`。 |
| 保存时头像关联 | PASS（前端流程回归） | 仅 `saveProfile()` 的资料 PUT 携带 `avatarFileId`。 |
| TypeScript 与双端构建 | PASS | `pnpm run typecheck`、临时注入本地 API 地址后的 H5/微信小程序生产构建均 exit 0。 |
| 数据库/Flyway | PASS（范围核对） | 本次没有 Schema 或资料保存逻辑变更。 |
| 真实 H5/MinIO 登录态 | NOT_RUN | 未执行真实用户资料写入或对象上传。 |
