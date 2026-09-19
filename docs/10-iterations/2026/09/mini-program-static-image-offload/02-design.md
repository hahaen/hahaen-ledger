# 设计

页面图片以统一 `staticResource(path)` 生成 `https://hahaen.xyz/minio-api/haji/wx/<path>`。在代码改动前，对全部 8 个对象执行匿名 HEAD 校验，HTTP 200 且远端 `Content-Length` 与原图一致。原图迁移至构建源目录外的归档路径，避免 `src/static/` 的自动拷贝。
