<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'
import { useLedger } from './stores/ledger'
const ledger = useLedger()
onLaunch(async () => {
  await ledger.restore()
  try { if (ledger.state.token) await ledger.refresh(); else await ledger.login() } catch { /* 页面仍可进入，后续由页面重试 */ }
  if (!uni.getStorageSync('first-use-complete')) setTimeout(() => uni.reLaunch({ url: '/pages/first-use/first-use' }), 0)
})
</script>
<template><slot /></template>
