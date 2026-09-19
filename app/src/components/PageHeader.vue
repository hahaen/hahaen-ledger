<script setup lang="ts">
import NativeNavigation from './NativeNavigation.vue'

withDefaults(defineProps<{ subtitle?: string }>(), { subtitle: '记录每一笔，让生活更清晰' })
</script>

<template>
  <NativeNavigation variant="brand" :subtitle="subtitle" :page-top-extra="20" />
</template>
