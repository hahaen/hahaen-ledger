<script setup lang="ts">
withDefaults(defineProps<{ title: string; closeOnBackdrop?: boolean }>(), { closeOnBackdrop: true })
const emit = defineEmits<{ close: [] }>()
function onModalTouchMove(event: Event) {
  // #ifdef H5
  event.stopPropagation()
  // #endif
}
</script>
<template>
  <view class="modal-backdrop">
    <view class="picker-touch-mask" @click.stop="closeOnBackdrop && emit('close')" @touchmove.stop.prevent />
    <view class="center-modal" role="dialog" aria-modal="true" :aria-label="title" @touchmove="onModalTouchMove">
      <view class="modal-heading"><text class="sheet-title">{{ title }}</text><button class="modal-close" aria-label="关闭弹窗" @click="emit('close')">×</button></view>
      <scroll-view scroll-y class="modal-content"><slot /></scroll-view><slot name="actions" />
    </view>
  </view>
</template>
