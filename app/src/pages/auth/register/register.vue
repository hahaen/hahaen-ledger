<script lang="ts">
// #ifdef MP-WEIXIN
import { createWechatShareMessage } from '../../../utils/wechatShare'
// #endif

export default {
  // #ifdef MP-WEIXIN
  onShareAppMessage: createWechatShareMessage,
  // #endif
}
</script>

<script setup lang="ts">
import AuthPage from '../../../components/AuthPage.vue'

</script>

<template><AuthPage mode="register" /></template>
