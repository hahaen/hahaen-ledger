# 第三轮工程审计报告

审计日期：2026-09-06。当前报告只记录已经检查到的事实，不把未执行命令写成 PASS。

## 范围

数据库主表/关联表分类、审计字段、中文 COMMENT、Flyway、Java Entity、MyBatis-Plus 自动填充、逻辑删除、统计、docs 重构、iterations、AGENTS、设计稿、核心链路、测试、构建、DEV 基础设施、安全，以及 Java 25/Netty 兼容性。

## 当前结果

- 2026-09-06 H5 登录/注册原型补充：V1 原型新增 H5 登录页、注册页、图形验证码展示/点击图片刷新、验证码错误、登录失败、提交禁用、退出登录和微信自动登录预览；两份产品文档已同步认证页面、路由和多端差异。本机浏览器已复验核心状态与路由，320/375/414 精确视口未执行；真实 H5 账号密码验证码接口当前不存在，因此该范围为 PARTIAL/BLOCKED，不影响微信小程序现有自动登录链路。

- 2026-09-06 Flyway 基线收口：当前完整数据库初始化统一为唯一 `V1__init_schema.sql`；原 V2/V3/V4 已完整吸收并从 Migration 目录移除。真实 DEV `haji_dev` 已在目录断言后执行 Flyway clean，从空库 migrate V1 并 validate；history 仅保留一个成功的 V1。六表字段、53 个公共/历史审计列、默认值/可空、中文 COMMENT、索引、外键、CHECK 约束、六表默认插入和事务回滚均 PASS。旧 V2/V3/V4 迭代文档保留为历史证据，不代表当前初始化顺序。

- 数据库：`V1__init_schema.sql` 现在一次性包含原 V2/V3/V4 的最终结构；Migration 目录不存在旧 V2/V3/V4。`AuditFieldDefaultsDevTest` 在真实 `haji_dev` 上完成 clean、migrate、validate 和 information_schema 核对，6/6 表、全部字段 COMMENT、索引、8 个外键、6 个 CHECK 约束及 history 单版本核对通过，状态 PASS。
- Java：已建立 `BaseAuditEntity`、`BaseRelationEntity`、自动填充处理器和删除审计入口；`mvn test` 与 `mvn clean package` 实际通过，V1 数据库重建、Schema 核对和应用启动验证 PASS。
- 文档：已建立 00-08 当前规范、09 审计、10 iterations 结构和本轮 iteration，状态 PARTIAL。
- 前端：修复 store 未接收首页/日历交易列表的问题并补齐品牌资产；`pnpm install --frozen-lockfile`、typecheck、H5、MP-WEIXIN 实际通过，lint 未配置。H5 本地可视检查发现 in-app 浏览器中 `#app` 为空，暂不能宣称运行时 UI PASS；设计逐页为 PARTIAL。
- 前端环境：已统一到 `app/env/`，`.env` 管理通用变量，development/production 文件对应 dev/prod；Vite envDir/loadEnv 使用同一目录。通用/环境/进程变量覆盖断言、typecheck、development H5、production H5/MP-WEIXIN 构建与生产地址注入通过，生产缺失 API 地址按预期失败，状态 PASS。详见 `app-env-directory` 迭代；真实生产域名和微信平台联调仍 BLOCKED。
- 安全：已把本地真实配置纳入 `.gitignore`，并将仓库配置改为环境变量占位；历史 Git 提交是否曾包含敏感信息需要人工检查，当前运行环境未做提交历史清理。
- 配置：`application.yml` 默认激活 `dev` Profile，并允许通过 `SPRING_PROFILES_ACTIVE` 覆盖；真实 DEV 基础设施连接仍未复验。
- Java 25/Netty：升级 Netty 到 `4.1.137.Final`，开发启动和打包 JAR 均成功启动；应用主入口已增加 `io.netty.noUnsafe=true` 兜底，未显式传入 JVM 参数的 JAR 启动也通过，Redis PING、MinIO Bucket 探测成功，应用日志不再出现 Netty `Unsafe` 警告，状态 PASS；本机 Maven 3.6.3 自身的 Jansi/Guava 警告仍为 PARTIAL。

## 未执行/阻塞

本次数据库范围已实际确认配置目标为 DEV `haji_dev`；V1 重建后的 Spring Boot 随机端口启动、Redis PING 和 MinIO Bucket 探测均 PASS，完整业务 HTTP 回归未执行。后端测试/构建结果记录在本次 V1 iteration。前端 lint 因项目未配置 script 为 NOT_RUN；真实微信登录因缺少可验证生产 AppID、合法域名和平台权限，BLOCKED。

## 人工重点验收

1. 在其他环境执行迁移前，使用当前唯一 V1 基线并核对所有业务表 TABLE_COMMENT、字段 COLUMN_COMMENT、NOT NULL、DEFAULT、索引和逻辑删除。
2. 回归新增/更新/删除/退款/还款/幂等、余额事务和双用户 IDOR。
3. 在微信开发者工具检查真实 `wx.login`、320/375/414 宽度、加载/失败重试和头像三态。

## 2026-09-06 H5 空白修复补充

已确认原因为缺少 H5 平台依赖，入口未生成挂载/路由，且 Vue shim 将生命周期钩子置空。补齐同版本 H5/微信平台包，恢复官方 Vue 运行时和类型，统一业务响应式导入，隐藏重复平台导航。localhost:5173 首次使用页、进入首页和四项导航实测 PASS，最终 typecheck 与两平台构建 PASS。产物确认 H5 assets 20 个文件、微信 9 个页面 WXML 及 app.json。

本次替代此前 H5 #app 空白结论；设计逐页及完整业务回归仍 PARTIAL，微信开发者工具运行 NOT_RUN，真实生产地址缺失 BLOCKED。生产缺失 API 校验仍有效，本次构建只临时使用本机 API。Sass legacy API 和微信 h1 样式选择器提示仍存在。详见 h5-blank-page 迭代。
