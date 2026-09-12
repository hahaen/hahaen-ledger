# 需求

H5 编译成功后，应以 `jenkins-deploy` 发布静态产物至既有 Nginx 根目录；不得因为无权限修改该目录权限而中断发布。

范围仅限 `app/Jenkinsfile` 的静态目录预检，不改变 Nginx 路由、后端部署或应用业务代码。
