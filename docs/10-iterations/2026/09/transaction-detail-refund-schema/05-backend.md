# 后端影响

本次不修改 Java Entity、Mapper、Service、Controller 或事务代码。

后续实现需使用 `BaseAuditEntity` 映射 `transaction_detail` 和 `transaction_refund`；两张表均有独立业务字段及逻辑删除生命周期，退款删除必须写删除审计字段。用户、账本、账户类型和逻辑删除校验必须在 Service 完成，不能信任前端 ID。
