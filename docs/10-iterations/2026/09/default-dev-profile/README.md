# 默认激活 DEV Profile

本次迭代修复后端直接启动时未自动加载 `application-dev.yml` 的问题。默认使用 `dev`，并保留通过 `SPRING_PROFILES_ACTIVE` 切换其他环境的能力。
