# 回滚

代码回滚时删除微信登录 Controller、Service、客户端和前端微信登录路径改动，恢复小程序原有认证行为；保留既有 `user_identity` 表和历史数据，不执行数据库删除。

配置回滚时将 `WECHAT_LOGIN_ENABLED` 设为 `false` 或移除开发/生产环境的微信开关，并重启服务。AppID 和 AppSecret 不写入版本库，凭证撤销或轮换在微信平台完成。
