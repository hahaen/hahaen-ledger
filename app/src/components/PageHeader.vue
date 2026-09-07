<script setup lang="ts">
withDefaults(defineProps<{ subtitle?: string }>(), { subtitle: '记录每一笔，让生活更清晰' })
</script>
<template>
  <view class="page-header"><image class="page-brand" src="/static/brand.png" mode="aspectFill" aria-label="哈记账" /><text class="page-subtitle">{{ subtitle }}</text></view>
</template>
<style scoped>
.page-header { flex-shrink:0; margin-bottom:20px; }
.page-brand { display:block; width:22px; height:22px; margin-bottom:8px; border-radius:7px; }
.page-subtitle { display:block; color:var(--muted); font-size:13px; line-height:normal; }
</style>
