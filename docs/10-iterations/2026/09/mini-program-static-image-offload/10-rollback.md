# 回滚

若公网对象不可用：将 `app/offloaded-static-assets/wx/` 迁回 `app/src/static/`，并将页面图片引用恢复为 `/static/...`，再重新构建。该回滚会恢复原先超过 2 MB 的小程序包，不能作为上传错误的最终解决方案。
