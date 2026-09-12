# 后端

- `dev` Profile：`logging.file.path=D:/github/log/haji`。
- `prod` Profile：`logging.file.path=/home/hahaen/log/haji`。
- 增加 YAML 配置加载测试，防止路径被后续修改或配置失效。

部署账户必须对 `/home/hahaen/log/haji` 具有创建目录与写入权限；目录首次启动时由 Spring Boot 创建。
