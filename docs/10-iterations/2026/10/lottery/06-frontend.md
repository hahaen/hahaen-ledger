# 前端

“我的”待办下面的入口只由当前profile.lotteryEnabled控制。4个子页面：lottery首页、lottery-editor单次/计划/续买/编辑、lottery-detail购买/计划/批次/开奖详情、lottery-draws本地历史。保留5项底部导航。

复用NativeNavigation、MoneyDisplay、EntryDateTimePicker和既有确认弹层样式；LotteryNumbers只在球号采用红蓝浅底，主视觉薄荷绿。沿用17px卡片圆角、细边框、轻阴影、固定底栏安全区和现有字阶。

所有请求通过utils/api.ts，utils/lottery.ts为类型及纯表单校验。失败保留输入和相同请求幂等键，改参数换键。存在未保存编辑时返回确认。所有新增页注册微信统一分享。首阶段真实页面验收原为NOT_RUN，后续已完成Chrome H5 320/375/414px及实际操作验收，证据见09-verification与evidence；微信运行验证按用户最新要求排除。
