# 回滚

## 未执行回滚

本次没有执行回滚操作，也没有删除或覆盖用户数据、容器、数据库或历史 Migration。

## 代码回滚范围

如需撤销本修复，只回退本迭代对应的：

- `app/src/pages/calendar/calendar.vue` 的日期格/日期数字节点。
- `app/src/prototype.scss` 的 `.calendar-cell` 和 `.day-num` 跨端盒模型声明。
- `app/tests/calendar-layout.test.mjs` 的新增回归断言。

不要回退工作区中本次之前已有的个人中心、全局样式、其他测试或文档改动。
