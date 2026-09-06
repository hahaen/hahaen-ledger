# 后端

同步 `AuditFieldDefaultsDevTest` 的当前基线断言为 Flyway V1，并增加对业务表、字段 COMMENT、索引、外键、CHECK 约束和单版本 history 的核对。Entity 与 Mapper 保持现有设计：五个主表继承 `BaseAuditEntity`，`UserIdentity` 继承 `BaseRelationEntity`，MyBatis-Plus 下划线转驼峰映射不变。

未进行与数据库版本收口无关的业务重构，也未修改配置文件。
