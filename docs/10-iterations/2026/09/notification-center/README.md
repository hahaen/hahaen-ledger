# 通知中心与 Key 回显（2026-09-30）

## 生产密钥配置示例补充（2026-09-30）

requirement：生产运行时需要提供固定的通知 Key 加密密钥。

design / database / api / backend / frontend：现有实现保持；`application.yml` 的 `hahaen.notification.key-aes-base64` 从 `NOTIFICATION_KEY_AES_KEY` 读取值。本次只为生产 env 示例增加无真实密钥的占位项。

testing / commands / verification：PASS（静态核对），已执行 `rg` 核对示例项、`application.yml` 的变量绑定与 Compose `env_file` 路径，并通过 `git diff --check`；未访问或修改生产服务器，生产运行状态 NOT_RUN。

rollback：删除示例占位项；若生产已有该密钥和密文，必须保留原密钥。

## requirement

通知配置从个人中心移至独立通知中心；“我的”页入口紧跟个人中心。再次进入时回显已保存的 Bark / pushplus Key，两项仍选填且没有小眼睛。

## design

沿用系统设置列表、自定义二级页头、白色圆角卡片、薄荷绿输入与固定保存按钮；通知入口用统一灰底铃铛轮廓。空值可以保存为移除，未修改字段不提交。资料页面恢复仅管理个人资料。

## database

继续使用已执行 V9，结构不变，不修改历史迁移。

## api

GET `/api/app/user/notification-configs` 返回当前有效用户的未删除配置，其中 `notificationKey` 是解密值，用于页面回显，响应头 `Cache-Control: no-store`。PUT 保持 RSA-OAEP 密文写入协议和幂等键，保存响应不回传 Key。

## backend

读取时依据 Sa-Token 当前用户和有效状态过滤；通过 `NotificationKeyCipher` 解密后构造 VO。主表继续保存 AES-256-GCM 密文，日志不记录 Key。归属校验、事务、删除审计与 V9 幂等关系表保持现有规则。

## frontend

新增 `pages/notification-center/notification-center.vue`、注册路由与统一微信转发；“我的”页在个人中心下方加通知中心。页面每次进入加载并回显，保存成功保留值且更新基线，失败保留原输入及相同密文/幂等键以重试，清空已保存值后保存移除。页面卸载清理内存，不写本地缓存。

## testing

Java 25 的 6 项通知定向/DEV 集成测试 PASS；真实 HTTP/DB 验证 Bark 与 pushplus 回显、跨用户隔离、删除后不再返回、重新配置、幂等重试、数据库仍为 AES 密文及禁止缓存响应头。使用隔离合成账号与 Key，数据已清理。前端类型检查、微信转发测试 18/18、H5 与微信生产构建均 PASS。

## commands

2026-09-30 实际执行：Java 25 `mvn -q -Dtest=NotificationKeyCipherTest,NotificationConfigServiceTest,NotificationConfigDevIntegrationTest -Dnotification.dev.verify=true test`（退出码0）；`pnpm run typecheck`（0）；`node --test tests/wechat-share.test.mjs`（18/18）；`pnpm run build:h5`（0）；`pnpm run build:mp-weixin`（0）。通过 IntelliJ 的 LedgerApplication 重启按钮载入新版后端，未改运行配置。

## verification

PASS：上述测试、构建与真实 API；现有登录态 H5“我的”页已显示入口顺序“个人中心 → 通知中心 → 关于与帮助”；直接访问通知中心实际展示两个选填输入、提示与固定保存按钮，无小眼睛。该账号当前两项为空，未向真实账号写入合成 Key。

PARTIAL：浏览器点击控制超时，H5 本轮通过直接路由核对画面；真实页面保存后再进入回显操作未执行，回显由隔离 DEV HTTP 测试验证。

NOT_RUN：微信开发者工具/真机交互和真实通知投递；本轮仅配置页面。

## rollback

回退本轮前端路由/入口/页面与读取 VO 变更即可恢复上一版，不改数据库 V9、不删除通知数据、不变更加密密钥。密钥继续在被忽略本地配置与部署环境中安全保管。

## 2026-09-30 追加：顶部通知中心卡片

requirement：通知中心顶部增加与个人中心同款引导卡。

design / frontend：复用 `profile-hero` 的薄荷渐变、圆角、标题和装饰圆环；标题为“通知中心”，表单沿用公共 20px 卡片间距。

database / api / backend：本次为页面展示调整，无变更。

testing / commands：实际执行 `pnpm run typecheck`、`pnpm run build:h5`、`pnpm run build:mp-weixin`，均退出码 0，PASS。此展示调整未增加业务测试。

verification：PASS（H5 实际页面），现有登录态通知中心显示薄荷渐变标题卡、装饰圆环、两个选填输入和固定保存按钮，卡片与表单保留公共间距；[截图](../../../../../app/tests/evidence/notification-center-card-20260930.png)。NOT_RUN：微信开发者工具及真机视觉，本次以微信生产构建验证编译兼容性。

rollback：移除新增引导卡并恢复原表单间距即可。
