# 08｜命令

本轮实际执行：

- `git check-ignore -v server/application-dev.local.yml`：确认本地配置命中忽略规则。
- Python 静态占位检查：确认凭证环境变量占位、optional import 和本地覆盖文件存在，输出 PASS。
- `git diff --check`：退出码 0。
- `mvn test -B -Dstyle.color=never`（`server/`）：退出码 1；编译阶段提示当前 JDK 不支持发行版本 25。
- `mvn -v`、`/usr/libexec/java_home -V`：唯一可用 JDK 为 17.0.3；没有 Java 25，因此测试用例未运行。
- 未启动应用或连接 MySQL、Redis、MinIO。

详见 `09-verification.md`。
