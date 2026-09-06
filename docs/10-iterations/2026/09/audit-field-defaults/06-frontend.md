# 前端

核对 `app/src/utils/api.ts` 和 `app/src/stores/ledger.ts`，现有 Account/Transaction/State 类型未声明或消费本次变更的公共审计字段，因此无需调整 TypeScript 字段或页面。请求参数、页面展示、业务日期字段不变。

本次前端构建、设计与微信运行 NOT_RUN；这是数据库公共字段任务，既有前端验收结果不由本次后端测试推定。
