# 实际执行记录（2026-09-28）

- git status --short --branch：main...origin/main [ahead 2]，开始时工作区干净；未提交、未推送、未改已有两个提交。
- /usr/libexec/java_home -v 25：找到Java25，后续Maven命令均指定JAVA_HOME。
- cd server && JAVA_HOME=本机Java25 mvn -q -Dtest=ItemServiceTest test：exit0，11项物品单测PASS。
- cd server && JAVA_HOME=本机Java25 mvn -q -Dtest=ItemServiceTest,ItemDevIntegrationTest -Ditem.dev.verify=true test：exit0，12项专测/真实集成测试PASS。DEV依赖启动成功；Flyway从V5迁移至V6，随后重跑已验证为V6。
- cd server && JAVA_HOME=本机Java25 mvn -q test：exit1，总65项，63通过、1失败、1默认跳过。失败为预先存在的LoggingProfileConfigTest：期望Windows开发日志目录，HEAD开发配置已为本机目录。本轮未修改该配置或该测试。默认跳过的真实DEV测试已在上条显式运行PASS。
- cd app && pnpm run typecheck：exit0。
- cd app && node --test tests/*.test.mjs：exit0，82/82 PASS。
- cd app && pnpm run build:h5 && pnpm run build:mp-weixin：exit0，双端构建完成；Sass旧API警告未阻断构建。
- node app/tests/visual-server.mjs：启动18761只读视觉夹具，不连接正式业务数据，拒绝全部写入。修复既有生产base路径和环境API替换后完成页面验证。
- CUA浏览器：320/375/414屏宽，导航、列表、当前成本折线、创建失败保留、退役和删除弹层、退役盈利场景验证；截图和尺寸JSON保存于evidence。

执行过程中两次在仓库根目录调用未指定pom的Maven命令因无pom失败，改为server目录/显式-f后完成；最初类型检查发现canvas可选宽度及switch事件类型错误，修复后通过。DEV测试初始夹具因无HTTP审计上下文、验证码字段名错误失败，已改为独立SQL创建合成用户和正式captchaId字段后通过，业务请求全程为真实HTTP。

真实账号、密码、验证码和会话仅在测试内存使用，不写commands或证据；清理仅限本轮随机合成用户/物品ID，不清空数据库或Redis。

- JAVA_HOME=本机Java25 mvn -q -f server/pom.xml -Dtest=ItemDevIntegrationTest -Ditem.dev.verify=true -Ditem.ui.verify=true test：exit0。临时18762页面桥接正式构建与真实DEV接口，使用测试内存中真实RSA登录会话，浏览器从页面新增/退役成功；通过target/item-ui-complete结束后关闭服务、logout并按精确ID清理合成数据。测试过程被中断/设备暂停后恢复，Hikari/Redis恢复连接成功，不将暂停日志当成业务失败。
- 临时只读18761服务已停止，浏览器测试标签已关闭，viewport已reset；原有8080 Java进程未中断。

- 最终修改后再次运行物品11项单测+真实DEV集成测试1项：exit0；新增逻辑删除用户的列表/详情/写入拒绝验证PASS。随后Java25 -DskipTests package：exit0。
- 最终前端typecheck、82项Node回归及H5/微信构建：exit0。原有8080进程保留；临时18761/18762均已关闭。
