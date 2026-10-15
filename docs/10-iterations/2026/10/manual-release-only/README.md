# 撤销自动发布并清理凭证（2026-10-15）

## requirement / design

用户撤销本次新增的 Jenkins 后端/H5 自动发布流程，要求删除凭证及模板，后续自行发版。保留原有 Review、测试、GitHub 同步/提交/推送规范；后端、H5 与微信发版由用户操作，代理不主动触发 Jenkins 或 Gitee 镜像同步。原有 Jenkinsfile、线上任务、Gitee 镜像及 GitHub Webhook不做变更。

## database / api / backend / frontend

无数据库、Migration、业务接口、后端/前端实现或生产配置变更，无需执行 SQL。本轮只移除工程规则和本地凭证/模板；不触发部署、发布或主动同步镜像。

## testing

只 Review 本次撤销、删除与文档差异；随后检查当前规范一致性、删除文件不存在、历史档案明确失效、本地链接及 Git 空白。纯规则/文档变更，业务测试、双端构建、DEV 运行与线上验收不适用。

## commands / verification

本次 Review 范围仅为发布规则撤销、凭证/模板删除、旧档案失效标记和审计记录；核对手动发版要求、Git 提交推送授权继续生效、原有 Pipeline 保留及不操作远端令牌，未发现未解决问题，进入纯文档/删除验证。PASS：文档规则一致性、凭证/模板/草稿及临时程序删除状态、本地链接/锚点和 `git diff --check` 检查通过；`git diff --exit-code -- server app docker-compose.prod.yml` 通过，原有业务和 Pipeline 无差异。纯文档/清理变更，业务测试、构建、DEV 及线上运行验收不适用；本轮无 PARTIAL/FAIL/BLOCKED。

已安全 stash 本轮文件及删除差异，`git pull --ff-only` 返回 Already up to date；恢复后文件 SHA-256 与删除状态完全一致，再删除对应备份。同步后复审本轮撤销差异无未解决问题，并复跑相同文档/清理检查。

历史发布实测只作为既往证据保留，不再表示当前授权。此前 Gitee 新令牌生成停在账号安全验证，未由代理完成生成或保存；本轮不操作远端账户令牌。

## rollback

如用户今后另行要求自动发布，需重新建立当次规则及独立凭证；不恢复已删除的本地真实 Key，不从 Git 历史提取凭证。原有手动发布配置不受本轮影响。
