# api

无 API 契约变更。首页仍调用 `/api/app/home/summary`，月份参数由前端当前日期生成；后端 `HomeService` 继续解析请求月份并按账务时区统计。
