# 后端

独立 item 域 Controller/Service/Mapper/Entity/DTO/VO，整分存储、BigDecimal 日均分计算，事务、用户行锁、归属、逻辑删除审计与请求内容冲突校验。不使用Redis缓存，沿用现有认证和Trace ID。

列表/详情JOIN有效app_user，禁止已逻辑删除或停用用户访问物品；写入锁定同样的有效用户行。真实DEV测试覆盖用户软删除后的读写拒绝，恢复仅针对合成测试用户。
