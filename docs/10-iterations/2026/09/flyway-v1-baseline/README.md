# `flyway-v1-baseline`｜V1 数据库基线

## 迭代目的

本迭代用于把当前开发阶段完整数据库初始化收口为唯一的 `V1__init_schema.sql`，明确 Flyway 迁移位置、基线策略、校验策略和禁止清库的保护措施。

## 应记录的重点

应说明基线包含哪些表、为什么旧开发 Migration 不再保留、非空开发库基线化的条件，以及如何核对 Flyway history、`information_schema` 和 Entity。后续正式结构变化必须新增版本，不修改共享环境已执行的 V1。

## 当前档案状态

当前工作区未保留本迭代明细文件；本 README 不是 Flyway 执行证明，真实迁移结果须有应用启动日志或数据库查询证据。
