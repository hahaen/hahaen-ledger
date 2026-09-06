# 命令

| 命令 | 结果 |
| --- | --- |
| `mvn -q -DskipTests compile` | PASS |
| `mvn -q test` | PASS |
| `pnpm run typecheck` | PASS |
| `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5` | PASS；仅通过进程环境变量提供构建地址 |
| `mvn spring-boot:run` | PASS；Flyway V1/V2 执行成功，Redis PING 成功，MinIO Bucket 可用 |
| H5 认证与 MinIO 联调脚本 | PASS；注册、登录、预签名 PUT、完成确认、预览 URL 均成功 |
| H5 MinIO 删除联调脚本 | PASS；删除后当前头像查询返回空，逻辑删除链路生效 |
