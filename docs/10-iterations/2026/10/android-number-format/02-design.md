# 02-design

扫描 app/src 的 locale/Intl 格式化调用。金额展示统一在 formatYuan 中按分四舍五入，再拆分整数/小数并手动分组，不依赖 Intl 或 toLocaleString 参数支持。保留图表坐标计算的精度及万/亿刻度。
