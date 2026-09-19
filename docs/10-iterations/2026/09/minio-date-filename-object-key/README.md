# `minio-date-filename-object-key`｜MinIO 上传对象 Key 命名

新上传头像使用按上海日期分组的对象目录，并在对象文件名中保留上传文件名主干和 UUID。

状态以 `09-verification.md` 为准。本轮无数据库结构/API 字段变化，历史对象 Key 不迁移；自动化测试、构建和真实 MinIO 联调未执行。

## 文件

按 `docs/10-iterations/README.md` 的 01–10 顺序记录需求、设计、数据库、API、后端、前端、测试、命令、验证和回滚。
