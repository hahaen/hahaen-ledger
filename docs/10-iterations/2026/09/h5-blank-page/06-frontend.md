# 前端

package.json 和锁文件补齐 @dcloudio/uni-h5、@dcloudio/uni-mp-weixin，固定 3.0.0-4080420251103001。移除 uni-vue-shim.ts 和 Vite Vue 自定义别名，删除四个不再直接使用的 @vue/* 依赖，五处业务导入改为 vue。BottomNav 挂载时隐藏平台导航。移除 env.d.ts 中覆盖 Vue 官方类型的声明，用 src/uni-components.d.ts 正确扩展组件 uni 属性。

