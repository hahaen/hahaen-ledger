# 06 前端

新增 `utils/nativeNavigation.ts`，仅在 MP-WEIXIN 分支同步读取系统安全区与 `uni.getMenuButtonBoundingClientRect()`，计算胶囊 top、height 和品牌区避让宽度。新增 `components/NativeNavigation.vue`，统一实现品牌页头、返回/标题栏、帮助标题栏和首次使用页品牌行。

首页、日历、资产、我的页的品牌图标和副标题在微信端同一水平行；新增/编辑记账、账单详情、账户详情、关于与帮助、协议页和个人中心的返回/标题栏按胶囊 top/height 定位。MP-WEIXIN 页顶只取状态栏/安全区高度较大值；H5 布局保持原样。未修改业务接口、账务或认证逻辑。
