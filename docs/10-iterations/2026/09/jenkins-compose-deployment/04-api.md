# 接口

本次不修改 Controller、DTO、VO 或 API 协议。公网 API 前缀为 `/haji-api/`；`location /haji-api/` 的 `proxy_pass http://haji-server:8080/` 会移除该前缀，因此浏览器请求 `/haji-api/api/app/...` 时，后端仍接收既有 `/api/app/...` 路径。H5 生产环境的 `VITE_API_BASE_URL` 必须指向站点源站加 `/haji-api`，不能直接设为根路径或后端容器地址。
