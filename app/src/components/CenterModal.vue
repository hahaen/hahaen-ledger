<script setup lang="ts">
defineProps<{ title: string }>()
const emit = defineEmits<{ close: [] }>()
</script>
<template>
  <view class="modal-backdrop" @click.self="emit('close')" @touchmove.stop.prevent>
    <view class="center-modal" role="dialog" aria-modal="true" :aria-label="title">
      <view class="modal-heading"><text class="sheet-title">{{ title }}</text><button class="modal-close" aria-label="关闭弹窗" @click="emit('close')">×</button></view>
      <scroll-view scroll-y class="modal-content" @touchmove.stop><slot /></scroll-view><slot name="actions" />
    </view>
  </view>
</template>
