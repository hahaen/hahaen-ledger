# 验证

| 项目 | 状态 | 依据 |
| --- | --- | --- |
| 本次代码Review | PASS | 范围见08-commands；权限、事务、幂等、审计、时间及平台兼容无未解决问题 |
| 前端全量回归 | PASS | 204/204，含实际编辑页逾期/旧单次/精度/旧周期、固定日、预览乱序和失败重试 |
| TypeScript及H5/微信命令行构建 | PASS | 三项均退出0，无微信开发者工具调用 |
| Java25完整verify | PASS | 130总/116执行通过/14条件跳过，0失败错误；6项计划单测通过 |
| 真实DEV HTTP/数据库 | PASS | 原有待办全链路及新增综合场景通过，含预览与保存一致、只读无写入、两个未完成项保留ID/时间/周期、历史不变、已发送记录保留、幂等和越权拒绝；新/改过去计划拒绝 |
| Flyway/Schema/Entity | PASS | 真实DEV既有集成检查通过，V12无新Migration；Redis/MinIO探测通过 |
| 实际H5端到端 | PASS | 独立合成账号真实后端：逾期单次改标题保存后DB仍2020-01-01T09:00:12.345及ONCE；新增每月1日保存与预览一致，完成后/固定日期展示正确 |
| 响应式/视觉 | PASS | 320/375/667横屏/768无横向溢出，375px截图见evidence，未改既有动效/主题；应用无控制台错误 |
| 验收清理 | PASS | H5测试结束退出0，合成账号/待办/登录日志清理；临时标签页、viewport和桥接停止 |
| upstream同步/复审复验 | PASS | 38文件SHA256恢复一致，远端Already up to date；复审无问题，后端22总/21通过/1可选跳过，前端204及类型复验通过 |
| 提交推送/服务器发布 | PASS | e55eda0已推送并核对；all退出0，后端/H5均实际发布，容器/公开资源/版本/JAR一致 |
| 微信工具/真机/上传发布 | NOT_RUN | 按用户阶段规则排除，非必需；用户自行发版 |

首轮新增测试清理遗漏登录日志外键曾FAIL，修正并复审、完整复验后已解决；无未解决必需FAIL/BLOCKED。UI控制台存在用户已有翻译扩展自身SyntaxError，来源chrome-extension，不属于应用，不写入截图或敏感内容。

发布闭环PASS：`e55eda0 fix(todo): 重构计划表单并保留逾期待办原计划`普通推送GitHub origin/main，`git ls-remote origin refs/heads/main`与当时本地HEAD一致；工作区干净后`python3 deploy/local/release.py all`退出0。后端与H5均于2026-10-31 16:15:35+08:00发布，分组件记录、latest-release及容器OCI revision均为e55eda053a4c08fa506127faa562a5f0e3cb238a；容器running/0重启。发布构建130总/113执行通过/17条件跳过、0失败错误；发布前真实DEV完整验证116通过、独立H5用例1/1通过。

公开HTML、13个静态资源（含待办清单/详情/编辑页JS与CSS、共享todoRepeat和入口）及haji-version.json逐字节匹配本地发布产物，静态版本067524c4-eddb-4223-b3d9-6c22f88e3557。服务器/app/app.jar与本地JAR的SHA256均为a23f4660fdcff7ed6f14c086dd30be81e0dca54d1bfab24e0a11a190aa3b1697。公开账户API及使用合法计划请求体的未登录preview API均HTTP401；首次用空对象核查preview得到参数校验400，改用合法计划后确认鉴权401，不以无效请求代替鉴权验证。

Chrome独立正式页面确认新增默认不重复；TIME以10-31为开始日期、每月1日时，正式后端预览11-01/12-01/次年01-01，375px无横向溢出。只切换空表单并调用只读预览，未保存或修改生产待办；正式截图见evidence/h5-production-preview-375.png。临时页面和viewport覆写已清理。

后端需要更新且已实际发布成功；H5需要更新且已实际发布成功，刷新可用；微信共享源码/类型/命令行构建已同步，使用新版需用户自行上传发布，工具/真机/上传发布NOT_RUN（按用户阶段规则排除，非必需）。数据库无需SQL/Migration/配置操作，V12材料摘要仍为2befcc2a90f9e19909e89f55cc1f54035c1726664ce810b7aec1cb5d38235980；保留数据、运行密钥、日志和现有备份。服务器仅余当前后端镜像，无ledger-upload或haji.incoming目录，不备份旧应用、不自动回滚。无未解决必需FAIL/BLOCKED；后续仅文档归档无需再次更新服务或客户端。
