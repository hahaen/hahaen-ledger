# testing

后端单元测试更新为断言显式条件软删除 Mapper 被调用，且不再调用 `updateById`；删除最后一笔退款后断言账单 `hasRefund=0`。真实已登录 DELETE 联调需在重启后端后执行。
