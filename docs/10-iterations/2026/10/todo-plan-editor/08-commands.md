# 实际命令

- git status --short：干净；git branch -vv：main ca7e4e8，upstream origin/main。
- 已读取AGENTS/README、待办规范、实现、相关测试及发布脚本；未输出本地凭证。

- Review失败回归：固定2020年逾期待办，直接执行实际编辑页脚本，断言回填及保存时间；测试未引入真实用户数据。
- `cd app && node --test --test-name-pattern='逾期编辑' tests/todo-repeat.test.mjs`：预期失败，实际回填2026-10-31而非2020-01-01；已复现。

首轮代码Review（2026-10-31）：仅本轮待办Controller/DTO/VO、计划辅助类、Service、归属过滤Mapper、编辑页、重复字段组件、API类型及相关回归。核对只读预览使用同一计算函数，禁止跨用户/完成项预览编辑；保留内容更新与重排事务/幂等边界、审计、ID及时间精度；对今天已过执行时刻从下一匹配项开始且锚点不变。未触及金额、账本、缓存、对象存储、部署脚本或历史迁移。无未解决问题，进入测试/构建/真实DEV验证。
- 首次归档命令误用app工作目录，未写入文档；已改回根目录。`node --test tests/todo-repeat.test.mjs`退出0，定向回归通过，日志仅本机/tmp/ledger-todo-plan-front-target.log。

- 前端204项全量回归、TypeScript、H5/微信项目命令行构建均退出0，日志/tmp/ledger-todo-plan-{front-all,type,h5,mp}.log。未调用微信开发者工具。
- 首轮真实DEV测试暴露新增测试清理遗漏app_login_log外键，业务断言已执行但清理报错，记为FAIL；已补按合成账号清理，并增加失败遗留清理，不扩大到真实账号。
- 复审修复及可选H5桥接：仅当前测试明确创建的todoplan账号；桥接端会话只存进程内存，不打印/写文件，浏览器沿用应用标准本机会话存储；绑定127.0.0.1，通过既有真实DEV后端访问，无新增生产入口。无未解决Review问题，进入后端复验和实际H5验收。

- Java25 `mvn -B -q -f server/pom.xml clean verify -Dtodo.dev.verify=true`复跑退出0：130总/116执行通过/14条件跳过，0失败错误；含6项计划边界单测、真实DEV既有完整待办HTTP、预览/多未完成项/历史/IDOR/幂等/毫秒/重排综合场景与Schema/Entity核验。MySQL/Redis/MinIO实际可用；Flyway当前V12且无待执行迁移。
- `mvn -B -q -f server/pom.xml test -Dtodo.dev.verify=true -Dtodo.ui.verify=true -Dtest=TodoApiDevIntegrationTest#realH5WithDisposableAccount`启动仅本机127.0.0.1:18764隔离桥接。Chrome实际H5：逾期单次仅改标题保存后原日期和单次均保留；新增TIME每月1日预览11-01/12-01/次年01-01，实际保存首项与预览一致；完成后仅预览本次、固定日期无重复开始字段且跨月两日预览正确。新任务通知关闭，无真实推送。
- Chrome 320×740、375×812、667×375、768×1024均无横向溢出；375px截图见evidence。控制台唯一错误来自用户已有翻译扩展clipboard.min.js，不是应用；应用无错误。临时标签页与viewport覆写已清理，已写完成信号让测试校验数据库并清理合成数据。

- `touch server/target/todo-ui-complete`后UI用例退出0：1/1通过，最终DB断言原毫秒时间、ONCE及修改后标题正确，finally清理完成。临时桥接停止。

同步后复审PASS：38个任务文件按SHA256保存清单；stash --include-untracked后`git pull --ff-only`返回Already up to date；按stash提交恢复，38文件逐字节一致、无冲突，确认后删除该备份。仅复审本轮计划校验/预览/内容保留/重排与前端状态、相关测试和文档；未发现未解决问题，无远端相关代码或依赖变化。进入受影响后端真实DEV与前端回归/类型复验，构建及H5运行证据仍对应相同源码，正式发布入口会再次完整构建后端/H5。

同步后复验PASS：Java25 `mvn -B -q -f server/pom.xml test -Dtodo.dev.verify=true -Dtest=TodoPlanningTest,TodoRepeatTest,TodoScheduleTest,TodoApiDevIntegrationTest,TodoDevIntegrationTest,TodoReminderWorkerTest`退出0：22总/21执行通过/1可选UI跳过，0失败错误；UI已在独立启用用例实际通过。前端204/204及TypeScript复跑退出0。日志/tmp/ledger-todo-plan-{backend-recheck,front-recheck,type-recheck}.log；文档相对链接、敏感字面量扫描和git diff --check通过。源码/依赖无新增变化，已执行双端构建及实际H5证据继续有效。准备逐文件暂存审查、提交推送后执行正式all发布。

- 逐文件暂存38个任务文件，审查git diff --cached、敏感字面量及git diff --cached --check通过；提交e55eda0后仅1个待推送提交，push origin HEAD:refs/heads/main成功，远端HEAD一致。
- `python3 deploy/local/release.py all`退出0；日志/tmp/ledger-todo-plan-release.log。
- `python3 /tmp/ledger-todo-plan-public.py`最终退出0（空preview请求的400校验修正为合法请求体检查401）；公开资源和版本匹配。
- SSH只读核对分组件发布记录、容器OCI revision/状态/重启次数、JAR SHA256、仅余当前镜像、无临时上传/静态站点目录，结果PASS；未读取或输出release.env等运行秘密。

发布闭环PASS：`e55eda0 fix(todo): 重构计划表单并保留逾期待办原计划`普通推送GitHub origin/main，`git ls-remote origin refs/heads/main`与当时本地HEAD一致；工作区干净后`python3 deploy/local/release.py all`退出0。后端与H5均于2026-10-31 16:15:35+08:00发布，分组件记录、latest-release及容器OCI revision均为e55eda053a4c08fa506127faa562a5f0e3cb238a；容器running/0重启。发布构建130总/113执行通过/17条件跳过、0失败错误；发布前真实DEV完整验证116通过、独立H5用例1/1通过。

公开HTML、13个静态资源（含待办清单/详情/编辑页JS与CSS、共享todoRepeat和入口）及haji-version.json逐字节匹配本地发布产物，静态版本067524c4-eddb-4223-b3d9-6c22f88e3557。服务器/app/app.jar与本地JAR的SHA256均为a23f4660fdcff7ed6f14c086dd30be81e0dca54d1bfab24e0a11a190aa3b1697。公开账户API及使用合法计划请求体的未登录preview API均HTTP401；首次用空对象核查preview得到参数校验400，改用合法计划后确认鉴权401，不以无效请求代替鉴权验证。

Chrome独立正式页面确认新增默认不重复；TIME以10-31为开始日期、每月1日时，正式后端预览11-01/12-01/次年01-01，375px无横向溢出。只切换空表单并调用只读预览，未保存或修改生产待办；正式截图见evidence/h5-production-preview-375.png。临时页面和viewport覆写已清理。

后端需要更新且已实际发布成功；H5需要更新且已实际发布成功，刷新可用；微信共享源码/类型/命令行构建已同步，使用新版需用户自行上传发布，工具/真机/上传发布NOT_RUN（按用户阶段规则排除，非必需）。数据库无需SQL/Migration/配置操作，V12材料摘要仍为2befcc2a90f9e19909e89f55cc1f54035c1726664ce810b7aec1cb5d38235980；保留数据、运行密钥、日志和现有备份。服务器仅余当前后端镜像，无ledger-upload或haji.incoming目录，不备份旧应用、不自动回滚。无未解决必需FAIL/BLOCKED；后续仅文档归档无需再次更新服务或客户端。

发布归档同步复审PASS：7个文档/证据文件安全stash，pull --ff-only为Already up to date，恢复后SHA256全部一致且无冲突，确认后删除备份。内容/链接及git diff --check复验PASS；仅文档，无应用源码差异，无需重复发布。
