# 设计

在 `application-dev.yml` 和 `application-prod.yml` 分别设置 Spring Boot 标准属性 `logging.file.path`。Spring Boot 会在对应目录创建并滚动维护默认文件日志，同时保留 `application.yml` 中现有的控制台格式与 Trace ID。

路径按 Profile 固定，生产路径不依赖开发机环境变量，也不会在默认配置中覆盖 Profile 选择。
