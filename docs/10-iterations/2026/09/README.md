# 2026 年 09 月迭代索引

本月档案集中记录业务域重置、数据库基线、用户、文件与资产账户表设计、资产账户排序、H5 认证/MinIO 接入，以及 H5 页面回归和运行环境整改。

首页、日历、资产三模块的业务实现、测试和验证证据见 [`home-calendar-assets/`](home-calendar-assets/)。该迭代明确不修改数据库结构和 Flyway。`docs-comprehensive-audit/` 记录 2026-09-08 对全部 `docs/` 的真实性审计、规范修正和剩余风险。

## 阅读建议

先看 `business-domain-reset` 了解工程起点，再按数据依赖阅读 `app-user-schema`、`file-storage-schema`、`asset-account-schema`、`transaction-detail-refund-schema`、`account-order`，随后阅读 `h5-auth-minio` 和各个 H5 修复迭代。`third-round`、`third-round-governance` 和 `docs-comprehensive-audit` 属于审计/治理记录，结论应与 `docs/09-audit/` 一起核对。

## 当前目录约定

完整功能迭代应包含固定的 01–10 文件；只有 README 的目录表示目前只保留了索引说明或历史明细尚未恢复，不能据此推断功能已经完成。

- [core-pages-prototype](core-pages-prototype/README.md)：首页/日历/记账/详情原型对齐、还款编辑和退款并发修复（2026-09-08）。

- [flyway-v5-checksum](flyway-v5-checksum/README.md)：恢复已执行 V5 内容，修复启动校验失败并实际启动验证。

- [database-initialization-adjustment](database-initialization-adjustment/README.md)：测试阶段重建数据库初始化基线，并统一 utf8mb4_general_ci。

- [home-static-month](home-static-month/README.md)：首页当前月份改为普通文字，移除刷新按钮。
- [home-summary-layout-current-month](home-summary-layout-current-month/README.md)：修复首页日均消费金额布局，并核对当前月份动态取值。
- [home-recent-list-design-alignment](home-recent-list-design-alignment/README.md)：对齐首页最近记账日期行和列表底色。
- [transaction-detail-header-alignment](transaction-detail-header-alignment/README.md)：账单详情页顶部导航与新增记账页对齐。
- [transaction-detail-delete-modal](transaction-detail-delete-modal/README.md)：账单详情删除确认弹窗与系统样式统一。
- [transaction-refund-modal-design](transaction-refund-modal-design/README.md)：账单详情退款弹窗按参考稿与系统样式对齐。
- `transaction-refund-modal-design` 后续补充退款记录删除闭环：删除成功后立即移除记录并回算详情，刷新失败不回显旧数据。
- [transaction-edit-type-lock](transaction-edit-type-lock/README.md)：账单详情编辑沿用原账单类型，不再显示新增记账的顶部类型切换。
- [calendar-records-scroll](calendar-records-scroll/README.md)：日历页固定上半部分，仅让记账记录区域独立滚动（2026-09-10）。
- [account-records-scroll](account-records-scroll/README.md)：资金账户和信贷账户详情固定筛选区，仅让日期与流水区域独立滚动（2026-09-10）。
- [mine-logout-modal-design](mine-logout-modal-design/README.md)：我的页退出登录确认弹层与系统圆角样式统一（2026-09-11）。
- [h5-logout-route-guard](h5-logout-route-guard/README.md)：H5 退出登录后仅允许访问登录页或注册页（2026-09-11）。
- [h5-authenticated-entry-home](h5-authenticated-entry-home/README.md)：H5 已登录用户重新打开默认登录/注册入口时直接进入首页（2026-09-13）。
- [profile-cumulative-days-from-first-transaction](profile-cumulative-days-from-first-transaction/README.md)：我的页累计记账天数改按最早有效账单的业务日期统计（2026-09-11）。
- [mine-profile-center-list-alignment](mine-profile-center-list-alignment/README.md)：我的页个人中心与关于帮助、退出登录统一为同一设置列表行样式（2026-09-11）。
- [profile-center-management](profile-center-management/README.md)：个人中心资料、首次账号设置和密码修改闭环（2026-09-11）。
- [avatar-object-key-dedup](avatar-object-key-dedup/README.md)：头像改存稳定对象 Key，上传内容校验和同图去重（2026-09-11）。
- [mine-avatar-preview-only](mine-avatar-preview-only/README.md)：我的页顶部头像仅展示，点击无响应（2026-09-12）。
- [profile-avatar-local-preview](profile-avatar-local-preview/README.md)：个人中心头像选择后立即本地预览，点击保存才关联当前头像（2026-09-12）。
- [profile-back-to-mine](profile-back-to-mine/README.md)：个人中心返回固定进入“我的”页（2026-09-12）。
- [profile-save-auto-return](profile-save-auto-return/README.md)：个人中心保存成功后展示 0.5 秒提示并自动返回“我的”页（2026-09-12）。
