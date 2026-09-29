# 命令

以下命令均在本轮最终实现后执行；前端命令在 `app/` 目录执行。

| 命令 | 结果 |
| --- | --- |
| `node --test tests/*.test.mjs` | PASS，83/83 |
| `pnpm run typecheck` | PASS |
| `pnpm run build:h5` | PASS |
| `pnpm run build:mp-weixin` | PASS |
| `JAVA_HOME=/Users/hahaen/Library/Java/JavaVirtualMachines/jdk-25.0.4.1.jdk/Contents/Home mvn -q -f server/pom.xml -Dtest=ItemServiceTest test` | PASS，exit 0 |
| `git diff --check` | PASS |

构建期间有 uni-app 更新提示及 Sass legacy JS API 弃用提示；未影响命令退出状态。
