# 后端

新增资料编辑 DTO、Controller PUT 路由、Service 事务和 Mapper 幂等记录访问。Service 锁定当前用户和目标物品，更新名称、分值、购买日期，不修改生命周期状态、退役信息或账户/账单。详情的成本数据由现有 `ItemCosts` 基于更新后的值重新计算。
