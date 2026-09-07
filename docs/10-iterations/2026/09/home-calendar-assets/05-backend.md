# 05｜后端

已完成的后端实现：

- Entity/Mapper 与 V3/V4 字段一一对应。
- 账户 Service 的类型形状、金额边界、归属、主动软删除和净资产汇总。
- 账单 Service 的四类账单影响、编辑先撤销旧影响、删除恢复、幂等和权限隔离。
- 退款 Service 的父账单锁、有效退款累计、`amount`/`has_refund` 重算和删除恢复。
- Home/Calendar/Asset 查询在 Service 层统一过滤 `user_id` 与 `deleted=0`。

新增代码位于 `server/src/main/java/com/hahaen/ledger/{account,asset,calendar,home,transaction}`。V3/V4 Entity 显式映射业务列；账户和账单写入使用 `@Transactional`，账户锁按 ID 排序，退款锁定父账单后更新有效金额。由于 V3 没有独立停用状态，账户删除使用 `deleted=1`，历史账单记录不物理删除。

限制：账户余额直接编辑没有独立“余额补齐流水”字段可写入，当前实现只更新现有余额列；真实 MySQL 事务、并发和隔离用户联调未执行，不能把单测等同于集成 PASS。
