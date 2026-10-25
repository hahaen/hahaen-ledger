<script setup lang="ts">
import { h, onMounted, ref, useAttrs, useSlots } from 'vue'
import { supportsIosCalculatorHaptics } from '../utils/iosCalculatorHaptics'

defineOptions({ inheritAttrs: false })
const props = defineProps<{ disabled: boolean }>()
const emit = defineEmits<{ click: [] }>()
const attrs = useAttrs()
const slots = useSlots()
const iosSwitch = ref(false)
onMounted(() => { iosSwitch.value = supportsIosCalculatorHaptics() })
function activate() {
  if (!props.disabled) emit('click')
}
// H5 使用真实按钮保留 Enter/空格与 disabled 的浏览器原生行为。
const KeyButton = () => h('button', {
  ...attrs, type: 'button', disabled: props.disabled, onClick: activate,
}, slots.default?.())

// 使用 h 创建真正的 HTML input，避免 uni-app 将模板 input 编译为 uni-input。
// 手指直接切换原生控件；不使用脚本 click、preventDefault 或自定义振动时序。
const HapticSwitch = () => h('input', {
  type: 'checkbox', switch: '', class: 'calculator-haptic-switch',
  disabled: props.disabled, tabindex: -1, 'aria-hidden': 'true',
  onClick: (event: MouseEvent) => { event.stopPropagation(); activate() },
})
</script>

<template>
  <view v-if="iosSwitch" class="calculator-key">
    <KeyButton />
    <HapticSwitch />
  </view>
  <KeyButton v-else />
</template>

<style>
/* 原生 button 不经过 uni-button 的样式重置；低优先级重置后沿用 .key 视觉。 */
:where(.keypad [type="button"]) { appearance:none; -webkit-appearance:none; margin:0; padding:0; border:0; border-radius:0; background:transparent; color:inherit; font:inherit; line-height:normal; }
:where(.keypad [type="button"]:not(:disabled)) { transition:transform .12s ease, opacity .12s ease, filter .12s ease; }
.keypad [type="button"]:disabled { opacity:.55; }
.keypad [type="button"]:active:not(:disabled) { transform:scale(.985); opacity:.86; filter:brightness(.99); }
.calculator-key { position:relative; width:100%; height:100%; min-width:0; min-height:0; }
.calculator-key > .calculator-haptic-switch { position:absolute; z-index:1; inset:0; display:block; width:100%; height:100%; margin:0; padding:0; opacity:0; appearance:auto; -webkit-appearance:auto; cursor:pointer; }
.keypad-pair .calculator-key + .calculator-key { border-left:1px solid #edf2f0; }
.calculator-key:has(> .calculator-haptic-switch:active:not(:disabled)) > .key { background:#eaf7f3; transform:scale(.985); opacity:.86; filter:brightness(.99); }
@media (prefers-reduced-motion:reduce) {
  .keypad [type="button"] { transition:none; }
  .calculator-key > .calculator-haptic-switch { pointer-events:none; }
}
</style>
