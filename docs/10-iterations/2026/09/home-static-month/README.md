# 首页月份文字与刷新入口调整

## requirement / design
按用户反馈：首页月份与日均消费标题为普通文字，年月取当前日期；移除最近记账右侧刷新按钮。

## database / api / backend
无变更。

## frontend
删除首页 MonthPicker 引用、弹窗状态、下拉标识及按钮。每次加载首页时更新当前月份，标题和查询月份一致。移除刷新按钮及专属样式，保留原有自动加载、下拉刷新和错误重试。

## testing / commands / verification
PASS：app 目录 pnpm run typecheck；临时设置本地 API 地址后 pnpm run build:h5。构建存在既有 Sass legacy-js-api 警告。未增加依赖或修改资产、我的和日历页。

## rollback
恢复首页月份选择控件和刷新按钮即可；无数据库操作。
