# 09｜验证

- PASS（静态）：跟踪的 dev 配置使用环境变量占位；本地账号密码不作为默认值写入。
- PASS（静态）：本地覆盖文件被 Git 忽略，示例模板只含占位符。
- PASS：`git diff --check` 无空白错误。
- BLOCKED：`mvn test -B -Dstyle.color=never` 在编译阶段失败，当前唯一可用 JDK 为 17.0.3，项目要求 Java 25；测试用例未运行。
- NOT_RUN：服务启动、MySQL/Redis/MinIO 连接。

## 实际执行的静态核对

- `git check-ignore -v server/application-dev.local.yml`：匹配 `.gitignore` 第 2 行。
- Python 静态占位检查：datasource、Redis、MinIO 凭证均使用环境变量占位；本地覆盖文件存在；两种启动目录的 optional import 保留。各项输出 PASS，未输出配置值。
- `git diff --check`：退出码 0。
- `git status --short`：本地覆盖文件未出现在可提交状态；工作区原有 `app/vite.config.ts` 修改仍在。

- 工具链证据：`mvn -v` 显示 Java 17.0.3；`/usr/libexec/java_home -V` 只列出该 JDK。
