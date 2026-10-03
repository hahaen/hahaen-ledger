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
import LegalDocumentPage from '../../../components/LegalDocumentPage.vue'

</script>

<template><LegalDocumentPage type="agreement" /></template>
