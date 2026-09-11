# 验证

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| V6 Migration 内容 | PASS（静态） | 旧 ID 回填为对象 Key、移除外键/列并新增去重索引。 |
| 上传与头像关联时序 | PASS（单元） | `AppFileServiceTest` 证明上传/去重不更新用户头像；`ProfileServiceTest` 证明仅资料保存携带自己的 READY 文件 ID 才写入对象 Key。 |
| 对象内容校验 | PASS（代码） | 完成接口核对大小、SHA-256、MIME 和文件头。 |
| 前端真实类型、SHA-256 与 READY 复用 | PASS（单元） | `utils/file.ts` 从文件头识别实际 JPEG/PNG/WebP/GIF 类型，计算摘要并跳过重复 PUT；覆盖声明 PNG 而实际 JPEG 的图片。 |
| 前端回归、类型检查与双端构建 | PASS | 页面回归 25/25、`vue-tsc` 及 H5/微信生产构建均已在保存后切换头像修复后执行成功。 |
| H5 文件选择器触发 | PASS（单元+构建） | 编译产物核对确认模板 file input 被改写；个人中心改为动态原生 file input，回归测试覆盖；未在真实登录会话中选择本地文件。 |
| MySQL/Flyway 实迁 | NOT_RUN | 尚未查询目标环境 history/schema。 |
| 真实 MinIO 上传、回读、同图复用 | NOT_RUN | 需要已登录会话、可用 MinIO 与 CORS。 |
| H5 个人中心运行验收 | BLOCKED | 本轮访问指定地址被 H5 路由守卫重定向至登录页，未取得可安全使用的登录会话。 |
| 微信运行验收 | NOT_RUN | 未执行微信开发者工具。 |
