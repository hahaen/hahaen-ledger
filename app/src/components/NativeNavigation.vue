<script setup lang="ts">
import { computed } from 'vue'
import { getNativeMenuMetrics } from '../utils/nativeNavigation'
import { staticResource } from '../utils/staticResource'

const props = withDefaults(defineProps<{
  variant: 'brand' | 'welcome' | 'screen' | 'help'
  title?: string
  subtitle?: string
  backLabel?: string
  backDisabled?: boolean
  compact?: boolean
  pageTopExtra?: number
  fullWidth?: boolean
}>(), {
  title: '',
  subtitle: '记录每一笔，让生活更清晰',
  backLabel: '返回',
  backDisabled: false,
  compact: false,
  pageTopExtra: 0,
  fullWidth: true,
})
const emit = defineEmits<{ back: [] }>()
const menu = getNativeMenuMetrics()

const menuAlignedStyle = computed(() => {
  if (!menu) return props.variant !== 'brand' && !props.fullWidth ? { marginLeft: '0px', marginRight: '0px' } : {}
  const pageTop = menu.rootInset + props.pageTopExtra
  const marginBottom = props.variant === 'brand' ? 20 : props.variant === 'welcome' ? 8 : props.variant === 'help' ? (props.compact ? 10 : 16) : (props.compact ? 10 : 20)
  return {
    marginTop: `${menu.top - pageTop}px`,
    marginBottom: `${marginBottom}px`,
    height: `${menu.height}px`,
    minHeight: `${menu.height}px`,
    paddingTop: '0px',
    paddingBottom: '0px',
    ...(props.variant === 'screen' ? { top: `${menu.top}px` } : {}),
    ...(['screen', 'help'].includes(props.variant) ? { marginLeft: props.fullWidth ? '-16px' : '0px', marginRight: props.fullWidth ? '-16px' : '0px' } : {}),
    ...(['brand', 'welcome'].includes(props.variant) ? { paddingRight: `${menu.rightPadding}px` } : {}),
  }
})
</script>

<template>
  <view v-if="variant === 'brand'" :class="['page-header', { 'menu-aligned': menu }]" :style="menuAlignedStyle">
    <image class="page-brand" :src="staticResource('brand.png')" mode="aspectFill" aria-label="哈记账" />
    <text class="page-subtitle">{{ subtitle }}</text>
  </view>
  <view v-else-if="variant === 'welcome'" :class="['welcome-brand', { 'menu-aligned': menu }]" :style="menuAlignedStyle">
    <image :src="staticResource('brand.png')" mode="aspectFill" aria-label="哈记账" />
    <text>哈记账</text>
  </view>
  <view v-else-if="variant === 'screen'" :class="['screen-nav', { 'menu-aligned': menu, compact }]" :style="menuAlignedStyle">
    <button data-tap-feedback="true" class="back nav-side" :aria-label="backLabel" :disabled="backDisabled" @click="emit('back')">‹</button>
    <text class="page-title">{{ title }}</text>
    <view class="nav-side" />
  </view>
  <view v-else :class="['help-nav', { 'menu-aligned': menu, compact }]" :style="menuAlignedStyle">
    <button data-tap-feedback="true" class="help-back" :aria-label="backLabel" @click="emit('back')">‹</button>
    <text class="help-title">{{ title }}</text>
    <view class="help-nav-side" />
  </view>
</template>

<style scoped>
.page-header { flex-shrink:0; margin-bottom:20px; }
.page-brand { display:block; width:22px; height:22px; margin-bottom:8px; border-radius:7px; }
.page-subtitle { display:block; color:#858b8b; font-size:13px; line-height:normal; }
.page-header.menu-aligned { display:flex; align-items:center; gap:8px; }
.page-header.menu-aligned .page-brand { flex:0 0 auto; margin:0; }
.page-header.menu-aligned .page-subtitle { min-width:0; overflow:hidden; white-space:nowrap; text-overflow:ellipsis; }
.welcome-brand { display:flex; align-items:center; gap:8px; margin-bottom:8px; color:#278879; font-size:18px; font-weight:700; }
.welcome-brand image { width:34px; height:34px; border-radius:11px; }
.welcome-brand.menu-aligned { gap:8px; }
.welcome-brand.menu-aligned image { width:26px; height:26px; flex:0 0 auto; border-radius:9px; }
.screen-nav { position:sticky; z-index:3; top:calc(var(--status-bar-height, 0px) + env(safe-area-inset-top, 0px)); display:flex; flex-shrink:0; align-items:center; justify-content:space-between; gap:10px; margin:0 -16px 20px; padding:8px 16px; min-height:62px; border-bottom:1px solid rgba(224,232,228,.7); background:rgba(247,248,247,.96); }
.screen-nav.compact { min-height:44px; margin-bottom:10px; padding:2px 16px; }
.screen-nav .page-title { flex:1; text-align:center; font-size:18px; font-weight:700; }
.nav-side { width:40px; flex-shrink:0; }
.screen-nav.compact .page-title { font-size:16px; }
.screen-nav .back { display:flex; width:40px; min-height:36px; height:36px; align-items:center; justify-content:flex-start; padding:0; border:0; border-radius:0; outline:0; color:#455651; background:transparent; box-shadow:none; font-size:24px; line-height:1; text-align:left; appearance:none; -webkit-appearance:none; }
.screen-nav .back::after { display:none; border:0; }
.help-nav { position:sticky; z-index:3; top:env(safe-area-inset-top, 0px); display:flex; align-items:center; justify-content:space-between; gap:10px; min-height:44px; margin:0 -16px 10px; padding:2px 16px; border-bottom:1px solid rgba(224,232,228,.7); background:rgba(247,248,247,.96); }
.help-nav:not(.compact) { margin-bottom:16px; }
.help-title { flex:1; font-size:16px; font-weight:700; text-align:center; }
.screen-nav.menu-aligned, .help-nav.menu-aligned { box-sizing:border-box; }
.screen-nav.menu-aligned { align-items:center; }
.screen-nav.menu-aligned .back { min-height:0; height:100%; }
.help-nav.menu-aligned { align-items:center; top:0; }
.help-back { display:flex; width:40px; min-height:36px; align-items:center; justify-content:flex-start; padding:0; border:0; border-radius:0; outline:0; color:#455651; background:transparent; box-shadow:none; font-size:24px; line-height:1; text-align:left; appearance:none; -webkit-appearance:none; }
.help-back::after { display:none; border:0; }
.help-nav-side { width:40px; flex-shrink:0; }
</style>
