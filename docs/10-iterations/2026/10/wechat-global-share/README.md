# 微信全局转发修复

日期：2026-10-02。

## requirement

个人开发者小程序右上角显示“当前页面不可转发”；所有页面应能转发小程序给好友/群，统一进入首页，不分享个人财务数据。

## design

现有工具函数间接调用 onShareAppMessage，20个页面编译产物均无 __runtimeHooks。uni-app 原生页面初始化只识别显式页面选项、编译标记或全局 mixin；工具回调没有变成微信原生方法。改用全局 mixin，固定标题、首页路径和既有品牌静态资源图片，避免默认页面截图泄露财务数据。朋友圈只能分享当前页，本轮不开放私有详情页朋友圈传播。

## database

无变更，不适用。

## api

无变更，不适用。

## backend

无变更；认证、权限、金额、事务、Redis、MinIO和Java验证不适用。

## frontend

应用初始化注册全局分享 mixin，移除20页手动注册；H5条件编译排除。复用 staticResource(brand.png)，不重复将726KB品牌图打入主包。

## testing

修复前：实际 uni-app 初始化函数生成的方法缺少 onShareAppMessage，回归 FAIL。修复后：该函数通过全局 mixin 生成转发方法并返回固定标题、首页和图片；H5无微信 mixin。完整前端130/130通过。微信开发者工具最终生产产物：首页、我的、帮助页菜单转发可用；首页及我的卡片标题和品牌图片正确，首页截图确认无财务截图。仅打开分享确认框后取消，没有发送消息。

## commands

已执行 git status --short --branch：main 跟踪 origin/main，工作区初始干净。检查微信编译产物：20页均无 __runtimeHooks；平台源码 initMixinRuntimeHooks 支持全局 mixin。

- node --test app/tests/wechat-share.test.mjs：修复前2项FAIL（原生回调缺失/待新增图片）；修复后2/2 PASS。最终采用既有固定图片URL，不新增本地图片。
- node --test app/tests/*.test.mjs：PASS，130/130。
- pnpm --dir app run typecheck：PASS，退出0。
- pnpm --dir app run build:h5 / build:mp-weixin：PASS，均退出0，仅既有Sass legacy-js-api提示。
- curl -I --max-time 20 品牌公开静态图片：HTTP200，image/png。
- 微信工具：重新编译最终生产产物，首页/我的/帮助菜单与前两页卡片通过；全部操作为只读/取消。

## verification

PASS：原生运行时回归、全量130项回归、类型检查、H5/微信生产构建、固定图片HTTP200、上述微信模拟器页面菜单与分享卡片。最终工具 Errors=0（返回我的页后Warnings计数13，未逐条核对其内容，不作为无告警证据）；重建产物清理期间工具短暂报app.json缺失，构建完成后重新编译恢复正常。

PARTIAL：全局接入已验证，工具只手动抽查3页，未逐一手动检查全部20页。

NOT_RUN（补充）：真实手机转发、好友接收后打开和线上发布版本验证；模拟器证据不能代表终端送达。未部署、上传或发布。

本轮必需回归、编译、类型及微信工具运行验证均已执行通过；真机与正式发布版本作为后续补充验收。

同步与最终复验：git stash push --include-untracked → git pull --ff-only（Already up to date）→ git stash apply，29个文件SHA-256全部一致后删除备份。同步后全量130/130与类型检查PASS、git diff --check通过。代码与依赖无变化，双端最终构建及微信工具验收结果继续适用。微信app.js包含全局mixin，H5产物不包含分享专用完整标题，条件编译检查PASS。最初使用“安心生活”短文案检查H5误命中帮助页既有标语，改为完整分享标题核验。

## 官方依据

- [微信官方Page类型与生命周期说明](https://github.com/wechat-miniprogram/api-typings/blob/master/types/wx/lib.wx.page.d.ts)：定义 onShareAppMessage 才显示转发按钮；接口说明没有个人主体限制。本轮不推断账号后台是否存在运营处罚或版本权限异常。
- [微信Page接口文档](https://developers.weixin.qq.com/miniprogram/dev/reference/api/Page.html)：本轮网络读取失败，采用官方源码说明及本地uni-app运行时证据。

## rollback

恢复本轮 main.ts、分享工具、各页面、测试和文档，恢复原分享图片配置。无数据回滚。
