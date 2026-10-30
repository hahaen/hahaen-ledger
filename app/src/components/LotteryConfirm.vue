<script setup lang="ts">
defineProps<{ title: string; copy: string; busy: boolean; confirm?: string }>()
defineEmits<{ cancel: []; confirm: [] }>()
</script>
<template>
  <view class="asset-create-backdrop" @click.self="!busy && $emit('cancel')" @touchmove.stop.prevent>
    <view class="account-delete-modal" role="dialog" aria-modal="true" :aria-label="title">
      <view class="asset-create-handle" /><text class="account-delete-title">{{ title }}</text><text class="account-delete-copy">{{ copy }}</text>
      <view class="account-delete-actions"><button class="account-delete-cancel" :disabled="busy" @click="$emit('cancel')">取消</button><button class="account-delete-confirm" :disabled="busy" :loading="busy" @click="$emit('confirm')">{{ confirm || '确认' }}</button></view>
      <slot />
    </view>
  </view>
</template>
