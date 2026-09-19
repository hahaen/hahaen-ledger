# 验证

| 项目 | 状态 | 证据/限制 |
| --- | --- | --- |
| 内网 Endpoint 与客户端 URL 映射 | PASS（代码/静态） | MinIO client 和签名输入继续用内网 Endpoint；PUT/GET URL 返回前替换为公网前缀，保留路径和查询签名。 |
| 自动化测试 | NOT_RUN | 本轮未执行。 |
| Maven 编译/打包 | PASS（仅构建） | `cd server; mvn -q -DskipTests package` exit 0。 |
| 差异格式检查 | PASS | `git diff --check` exit 0。 |
| 正式 Nginx 配置与签名 GET/PUT | NOT_RUN | 未登录生产主机或验证运行时代理。 |
| H5/微信文件回显 | NOT_RUN | 未在正式客户端实测。 |
