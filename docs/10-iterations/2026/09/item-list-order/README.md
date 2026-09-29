# item-list-order｜物品清单排序

## 目标与范围

物品清单优先显示正在服役的物品；同一状态组内按购买日期由新到旧排列，使服役时间较短的物品靠上、时间较长的靠下。同日按 ID 倒序保证顺序稳定。排序在后端列表查询中完成，筛选和分页沿用统一顺序。

## 结论

Mapper 列表查询和物品 API 契约已更新。未修改数据库、请求/响应字段或成本计算。自动化测试、类型检查、构建和运行态验收未执行。

## 文件导航

- [需求](01-requirement.md) · [设计](02-design.md) · [数据库](03-database.md) · [API](04-api.md)
- [后端](05-backend.md) · [前端](06-frontend.md) · [测试](07-testing.md) · [命令](08-commands.md)
- [验证](09-verification.md) · [回滚](10-rollback.md)
