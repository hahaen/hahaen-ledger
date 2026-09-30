# 本地后端端口 9898

- requirement：用户明确指定后端本地端口为 9898，正式环境不变。
- design：仅 dev Profile 的 SERVER_PORT 默认值由 8080 改为 9898；前端 development API 同步。H5 服务仍为 5180。
- database / api：无结构或接口契约变更。
- backend：application-dev.yml 端口默认值修改；application.yml 和 application-prod.yml 无差异。
- frontend：app/env/.env.development 指向 http://127.0.0.1:9898；生产环境文件和 Vite 配置无差异。
- testing / commands：Java 25 下 mvn -q -DskipTests package 退出 0；mvn -q spring-boot:run 启动，Tomcat 实际监听 9898；GET /api/app/auth/password-key 返回 200；Swagger 返回 302；来自 localhost:5180 的 OPTIONS 返回 200 并允许该 Origin。Python 核对开发/生产配置隔离 PASS。
- verification：本地后端启动、公开接口、H5 跨域许可 PASS。首次探测 /actuator/health 返回 500，该项目未提供此健康入口，改用实际认证公开接口验收。业务全量回归不适用（只变更开发连接端口）；当前没有进行登录或写入业务数据。前端类型检查、H5 和微信构建在撤回误改 H5 端口前通过，最终前端仅 development API 地址变化，生产构建输入未改变。
- rollback：将 dev 后端默认端口及 development API 恢复为 8080 后重启；正式环境无须回退。
