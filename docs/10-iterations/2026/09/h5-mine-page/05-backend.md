# 后端

新增 `ProfileController`、`ProfileService` 和 `ProfileVO`，由 Service 从 `AppUserMapper` 读取当前用户，校验状态和逻辑删除标识，并计算累计自然日；Controller 只做协议适配。

调整认证会话退出：`H5` 调用方由页面决定导航，服务层负责请求退出和本地状态清理；微信条件编译路径保留自动重新登录行为。

头像服务继续使用 `MinioStorageService`，不向前端暴露 Bucket、Access Key、Secret Key 或永久 URL。文件归属查询补充逻辑删除过滤，避免已删除文件重新被读取。
