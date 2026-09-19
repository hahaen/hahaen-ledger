# 验证

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 新对象 Key 生成格式 | PASS（静态） | 服务端基于当前用户 ID、现有 `originalName`、上海日期、UUID 和 MIME 扩展名生成新 Key。 |
| API/数据库兼容 | PASS（静态） | 无 API 字段或 Schema 变化；现有对象和引用不迁移。 |
| 自动化测试、类型检查和构建 | NOT_RUN | 本轮未执行。 |
| 真实 MinIO 上传、确认和预览 | NOT_RUN | 本轮未执行。 |
