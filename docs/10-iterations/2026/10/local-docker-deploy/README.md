# 本机产物直传与 Jenkins 退役

## requirement

2026-10-25 用户明确要求卸载 my-134 的 Jenkins、删除两个 Gitee 仓库及自动推送配置，改成本机编译上传、Docker 更新；成功后只保留最新发布产物。数据库、对象存储和运行配置保留。

## design

本机 Java 25/Maven 编译 JAR，pnpm 编译 H5；仅上传编译产物、运行 Dockerfile、Compose 和发布脚本。服务器不拉取 Git、不安装构建依赖。共用网络迁移到独立业务网络后卸载 Jenkins。

## database / api / backend / frontend

无业务接口或 Schema 修改；Flyway 原有启动迁移保持。微信小程序不上传、不提审、不发布。后端和 H5 将以新流程验收。

## testing / commands / verification

Review 与验证结果见下方记录；正式新版本上线和最终清理尚未执行。

## rollback

发布期间临时保留旧镜像/静态目录；健康检查失败恢复旧运行配置。成功后删除旧产物，之后如需回退须从 Git 选定版本重新构建。数据库迁移失败不自动反向执行 SQL。


## 首轮与 upstream 同步后复验

PASS：本轮差异 Review 与复审；修正 Java 25 选择、真实 HTTP 401 探针、仅 SQL/JSON 迁移摘要、构建前后源码一致性检查。明确逐项目锁、私有配置边界、失败恢复及最新产物清理范围；无未解决问题。

PASS：逐文件 stash 保存与 SHA-256 恢复核对，`git pull --ff-only` 返回 Already up to date；无冲突，恢复核对后删除本轮 stash。记账首次拉取遇到 SSH 连接中断，重试成功后才恢复改动，未跳过同步。

PASS：每项目 4 项上传产物安全测试、Bash/Python 语法、差异空白检查；电台文档 194 文件、1413 本地链接与变更测试的 ESLint 检查。电台 upstream 复验 436 项 PASS、15 项数据库环境专项跳过，生产构建 PASS；记账 Java 25 `mvn verify` 90 项 PASS、6 项依赖专用开发环境测试跳过，H5 类型检查/生产构建 PASS。此次无数据库结构或业务代码变更，数据库写入专项不适用。

PASS：本机产物上传后，在服务器构建 Linux/amd64 运行镜像；电台模块导入/运行依赖检查通过，记账 Temurin 25.0.4.1 运行镜像检查通过。电台迁移镜像只读核对 pending=[]，本地迁移材料一致；记账当前运行 JAR 与本机 JAR 的 11 份迁移完全一致，无需生产 SQL。

PARTIAL：业务网络已迁移到 `haen_application`，保留 172.20.0.1 网关，Nginx 配置及两服务 HTTP 检查通过。实际新版本上线与最终清理结果将在完成后追加。

复审修正：在替换网页前即设置恢复标记，复制失败时也能从事务备份恢复旧网页。Python 安全测试与 Bash 语法再次通过。
