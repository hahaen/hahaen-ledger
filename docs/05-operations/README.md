# 运行与基础设施规范

DEV 使用 Spring Profile `dev`、MySQL `haji_dev`、独立 Redis 和 MinIO Bucket `haji-dev`；真实凭证只放被忽略的 `application-dev.yml`。当前数据库初始化唯一基线是 `V1__init_schema.sql`，应用启动会自动执行并校验 V1。开发数据库重建必须先确认 catalog 为 `haji_dev`，再通过 Flyway clean 后 migrate V1；禁止手工修改 `flyway_schema_history` 冒充成功。Redis Key 使用 `haji:` 业务命名空间且临时 Key 必须有 TTL；MinIO 通过 `MinioStorageService` 使用短时 URL。启动、Flyway、Redis 和 MinIO 的真实结果必须写入审计证据。
