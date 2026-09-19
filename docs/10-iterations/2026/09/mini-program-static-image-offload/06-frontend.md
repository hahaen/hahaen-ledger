# 前端

- 新增 `app/src/utils/staticResource.ts`，集中管理固定视觉资源的公开根地址。
- 品牌、账户、账单类型图片的所有原 `/static/...` 引用改为统一函数调用。
- 原始 8 张图片从 `app/src/static/` 移至 `app/offloaded-static-assets/wx/`，以保留副本且避免构建携带。
