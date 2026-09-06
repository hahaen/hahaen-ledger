# 测试

PASS：最终 typecheck；H5 生产模式构建；微信构建，app.json 存在且有 9 个页面 WXML；H5 产物 assets 有 20 个文件。

PASS：浏览器首次使用页可见，点击开始记账进入首页，日历、资产、我的切换成功；自定义导航保留四项且平台导航不再重叠。重启后验证阶段无新增 console error/warn。

PARTIAL：全量设计稿逐页验收及业务 HTTP 回归未覆盖。NOT_RUN：微信开发者工具运行。BLOCKED：真实生产地址未提供，构建使用临时本机地址。

