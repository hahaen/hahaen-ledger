<script setup lang="ts">
import { computed, getCurrentInstance, nextTick, onBeforeUnmount, onMounted, watch } from 'vue'
import { type CostPoint } from '../utils/items'
import { formatYuan } from '../utils/money'
import { itemCostScale } from '../utils/itemCostChart'
const props = defineProps<{ points: CostPoint[] }>()
const instance = getCurrentInstance()
const scale = computed(() => itemCostScale(props.points.map(point => Number(point.dailyCostCents))))
const dateLabel = (date: string) => props.points[0]?.date.slice(0, 4) === props.points[props.points.length - 1]?.date.slice(0, 4) ? date.slice(5) : date
function axisLabel(cents: number) {
  const yuan = cents / 100
  if (Math.abs(yuan) >= 100000000) return `${(yuan / 100000000).toFixed(1)}亿`
  if (Math.abs(yuan) >= 10000) return `${(yuan / 10000).toFixed(1)}万`
  return formatYuan(cents)
}
async function draw() {
  await nextTick()
  uni.createSelectorQuery().in(instance?.proxy).select('.item-cost-canvas').boundingClientRect(rect => {
    if (!rect || Array.isArray(rect) || typeof rect.width !== 'number') return
    const width = rect.width, height = 192, left = 54, right = width - 12, top = 18, bottom = 162
    const ctx = uni.createCanvasContext('item-cost-history', instance?.proxy)
    const values = props.points.map(point => Number(point.dailyCostCents))
    if (!values.length) return
    const y = (value: number) => bottom - scale.value.position(value) * (bottom - top)
    const first = props.points[0], last = props.points[props.points.length - 1]
    const x = (day: number) => left + (day - first.day) / Math.max(1, last.day - first.day) * (right - left)
    ctx.clearRect(0, 0, width, height)
    ctx.setFontSize(10); ctx.setTextAlign('right')
    for (const value of scale.value.ticks) {
      const pos = y(value)
      ctx.setStrokeStyle('#e9efec'); ctx.setLineWidth(1); ctx.beginPath(); ctx.moveTo(left, pos); ctx.lineTo(right, pos); ctx.stroke()
      ctx.setFillStyle('#858b8b'); ctx.fillText(axisLabel(value), left - 7, pos + 3)
    }
    ctx.setStrokeStyle('#49ad9c'); ctx.setLineWidth(2.5); ctx.beginPath()
    props.points.forEach((point, i) => { if (!i) ctx.moveTo(x(point.day), y(Number(point.dailyCostCents))); else ctx.lineTo(x(point.day), y(Number(point.dailyCostCents))) })
    ctx.stroke()
    for (const point of [first, last]) { ctx.beginPath(); ctx.arc(x(point.day), y(Number(point.dailyCostCents)), 3.5, 0, Math.PI * 2); ctx.setFillStyle('#278879'); ctx.fill() }
    ctx.setFillStyle('#858b8b'); ctx.setTextAlign('left'); ctx.fillText(dateLabel(first.date), left, 185)
    if (props.points.length > 1) { ctx.setTextAlign('right'); ctx.fillText(dateLabel(last.date), right, 185) }
    ctx.draw()
  }).exec()
}
function resized() { void draw() }
onMounted(() => { void draw(); uni.onWindowResize(resized) })
onBeforeUnmount(() => uni.offWindowResize(resized))
watch(() => props.points, () => { void draw() }, { deep: true })
</script>
<template><view><canvas id="item-cost-history" canvas-id="item-cost-history" class="item-cost-canvas" role="img" aria-label="日均成本随服役日期变化的折线图" /><text v-if="scale.logarithmic" class="item-cost-scale-note">纵轴为非等距刻度，便于查看长期变化</text></view></template>
<style scoped>.item-cost-canvas { width:100%; height:192px; } .item-cost-scale-note { display:block; margin-top:4px; color:#858b8b; font-size:11px; line-height:1.6; }</style>
