# 命令

| 命令 | 结果 |
| --- | --- |
| `git diff --check` | PASS：退出码 0；仅出现仓库既有 CRLF/LF 转换提示，没有空白错误。 |

未在本地伪造 Jenkins 或远程 Docker 发布验证。

后续 Jenkins 实际运行已确认构建和目录预检通过；`cp -a` 因无法保留 `/home/hahaen/nginx/html/.` 时间戳退出。该日志是故障定位证据，不作为修复后发布通过的证据。
