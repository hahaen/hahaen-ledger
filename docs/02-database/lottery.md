# 独立彩票数据模型

V12新增app_user.lottery_enabled（NOT NULL DEFAULT 0，0关闭1开通），既有和新建账号全部默认关闭，不增加角色，不通过普通资料接口写入。

官方共享表：lottery_draw按彩种/期号唯一，存号码、分金额奖金表、规则状态和修订版本；lottery_draw_revision留官方响应证据；lottery_sync_state保存同步租约及补漏/循环复查游标。

个人业务表：lottery_plan长期号码默认值；lottery_batch每次确认的1–20期实际购买；lottery_entry逐期号码、倍数、成本快照与结算；不关联账本。

提醒表：lottery_event业务事件快照，lottery_delivery各渠道状态，lottery_attempt逐次尝试。lottery_request为幂等关系表。主表继承十个审计字段，关系表仅created_at/deleted，均有中文注释。

未知奖金为NULL；未中奖为0。删除个人计划级联软删其个人记录并停止未发提醒，不删除共享开奖。金额按整数分。

正式结构以V12为准；已执行迁移禁止回改，后续结构变化另建Vn。

权限列在MyBatis实体上禁止业务INSERT/UPDATE写入：创建依赖DEFAULT0；资料保存的旧实体不能覆盖维护者并发撤权。后续仅由维护者显式数据库配置。
