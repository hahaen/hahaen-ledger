# 07｜测试

测试代码放在 `server/src/test/java`，本轮已执行：

- `MoneyUtils`/日期工具：金额分转换、上限、日期月边界、闰年和 6 行日历计算。
- `AccountServiceTest`：FUND/CREDIT 字段形状、额度/欠款边界、归属和删除；`AssetServiceTest`：净资产、净资产标识和账户列表。
- `TransactionServiceTest`：四类账单金额影响、编辑、删除、幂等、越权、逻辑删除和非法账户。
- `TransactionServiceTest`：部分退款、转账/还款账户规则、余额不足、未知账单和退款余额恢复；退款逻辑属于该 Service，暂未拆出独立 `RefundServiceTest`。
- `HomeServiceTest`/`CalendarServiceTest`：统计口径、日期边界、排序、月/日隔离。
- Controller 测试：参数校验、响应结构和未登录/非法请求；真实数据库事务测试仅在隔离 MySQL 可用时执行。

`mvn test` 最终结果为 18 tests，0 failures/errors/skipped。测试是 Mockito Service/纯逻辑层，尚未包含真实 MySQL Mapper/事务集成；独立端口启动时 Redis PING 和 MinIO bucket 探针成功，未创建真实账务测试数据，微信开发者工具未运行。
