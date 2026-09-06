# 设计

在基础配置中设置 `spring.profiles.active: ${SPRING_PROFILES_ACTIVE:dev}`。Spring Boot 会按 Profile 约定自动合并 `application-dev.yml`，环境变量可覆盖默认值。
