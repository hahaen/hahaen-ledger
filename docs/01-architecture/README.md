# 架构规范

项目采用模块化单体：uni-app 页面和 store 负责交互，Java Service 负责认证、归属校验、账务事务和统计，MyBatis-Plus Mapper 负责持久化。金额使用整数分，账本时区用于自然月/自然日统计，写入接口使用幂等键。Controller 不承担业务规则；所有业务资源从 Sa-Token 当前用户推导。
