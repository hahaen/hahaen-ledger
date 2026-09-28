<script setup lang="ts">
import { getCurrentInstance, nextTick, onBeforeUnmount, onMounted, watch } from 'vue'
import { type CostPoint } from '../utils/items'
import { formatYuan } from '../utils/money'
const props = defineProps<{ points: CostPoint[] }>()
const instance = getCurrentInstance()
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
    const max = Math.max(0, ...values), min = Math.min(0, ...values), range = max - min || 100
    const y = (value: number) => bottom - (value - min) / range * (bottom - top)
    const first = props.points[0], last = props.points[props.points.length - 1]
    const x = (day: number) => left + (day - first.day) / Math.max(1, last.day - first.day) * (right - left)
    ctx.clearRect(0, 0, width, height)
    ctx.setFontSize(10); ctx.setTextAlign('right')
    for (let i = 0; i < 3; i++) {
      const value = min + range * i / 2, pos = y(value)
      ctx.setStrokeStyle('#e9efec'); ctx.setLineWidth(1); ctx.beginPath(); ctx.moveTo(left, pos); ctx.lineTo(right, pos); ctx.stroke()
      ctx.setFillStyle('#858b8b'); ctx.fillText(axisLabel(value), left - 7, pos + 3)
    }
    ctx.setStrokeStyle('#49ad9c'); ctx.setLineWidth(2.5); ctx.beginPath()
    props.points.forEach((point, i) => { if (!i) ctx.moveTo(x(point.day), y(Number(point.dailyCostCents))); else ctx.lineTo(x(point.day), y(Number(point.dailyCostCents))) })
    ctx.stroke()
    for (const point of [first, last]) { ctx.beginPath(); ctx.arc(x(point.day), y(Number(point.dailyCostCents)), 3.5, 0, Math.PI * 2); ctx.setFillStyle('#278879'); ctx.fill() }
    ctx.setFillStyle('#858b8b'); ctx.setTextAlign('left'); ctx.fillText(first.date.slice(5), left, 185)
    ctx.setTextAlign('right'); ctx.fillText(last.date.slice(5), right, 185)
    ctx.draw()
  }).exec()
}
function resized() { void draw() }
onMounted(() => { void draw(); uni.onWindowResize(resized) })
onBeforeUnmount(() => uni.offWindowResize(resized))
watch(() => props.points, () => { void draw() }, { deep: true })
</script>
<template><canvas id="item-cost-history" canvas-id="item-cost-history" class="item-cost-canvas" role="img" aria-label="日均成本随服役日期变化的折线图" /><view class="chart-accessible">{{ points.length ? `从${points[0].date}的${formatYuan(points[0].dailyCostCents)}元/天，到${points[points.length - 1].date}的${formatYuan(points[points.length - 1].dailyCostCents)}元/天` : '暂无成本数据' }}</view></template>
<style scoped>.item-cost-canvas { width:100%; height:192px; }.chart-accessible { color:#858b8b; font-size:11px; line-height:1.7; margin-top:8px; overflow-wrap:anywhere; }</style>
