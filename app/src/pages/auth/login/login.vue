<script setup lang="ts">
import AuthPage from '../../../components/AuthPage.vue'
import { registerWechatShare } from '../../../utils/wechatShare'

registerWechatShare()
</script>

<template><AuthPage mode="login" /></template>
