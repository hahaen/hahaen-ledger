# 本地开发凭证配置

## 迭代目的

将 dev Profile 的数据库、Redis、MinIO、CORS、H5 RSA 与微信本机连接/认证值移出被 Git 跟踪的配置文件，避免提交个人连接信息和账号密码。

## 当前状态

- `application-dev.yml` 保留安全本机默认值和环境变量引用，不包含数据库/Redis/MinIO 账号或密码。
- 本机覆盖文件为 `server/application-dev.local.yml`，由现有可选配置导入加载并由 `.gitignore` 忽略。
- 仓库模板为 `server/application-dev.local.example.yml`，只含明确占位符。
- 静态配置核对与 `git diff --check` 为 PASS。Maven 测试因本机仅有 Java 17.0.3、项目要求 Java 25 而 BLOCKED；应用启动和基础设施连接未执行。

## 明细

需求、设计、配置影响、验证与回滚分别记录在本目录 `01`–`10` 文档。
