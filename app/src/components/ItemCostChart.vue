<script setup lang="ts">
import { computed, getCurrentInstance, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { type CostPoint } from '../utils/items'
import { formatYuan } from '../utils/money'
import { itemCostScale } from '../utils/itemCostChart'
const props = defineProps<{ points: CostPoint[] }>()
const instance = getCurrentInstance()
const plotWidth = ref(240)
const scale = computed(() => itemCostScale(props.points.map(point => Number(point.dailyCostCents))))
const dateLabel = (date: string) => props.points[0]?.date.slice(0, 4) === props.points[props.points.length - 1]?.date.slice(0, 4) ? date.slice(5) : date
function axisLabel(cents: number) {
  const yuan = cents / 100
  if (Math.abs(yuan) >= 100000000) return `${(yuan / 100000000).toFixed(1)}亿`
  if (Math.abs(yuan) >= 10000) return `${(yuan / 10000).toFixed(1)}万`
  return formatYuan(cents)
}
const vertices = computed(() => {
  const first = props.points[0], last = props.points[props.points.length - 1]
  if (!first || !last) return []
  return props.points.map(point => ({
    x: (point.day - first.day) / Math.max(1, last.day - first.day) * plotWidth.value,
    y: (1 - scale.value.position(Number(point.dailyCostCents))) * 144,
  }))
})
const segments = computed(() => vertices.value.slice(1).map((point, index) => {
  const start = vertices.value[index]
  const dx = point.x - start.x, dy = point.y - start.y
  return { left: `${start.x}px`, top: `${start.y}px`, width: `${Math.hypot(dx, dy)}px`, transform: `rotate(${Math.atan2(dy, dx)}rad)` }
}))
const endpoints = computed(() => vertices.value.length > 1 ? [vertices.value[0], vertices.value[vertices.value.length - 1]] : vertices.value)
async function measure() {
  await nextTick()
  uni.createSelectorQuery().in(instance?.proxy).select('.item-cost-plot').boundingClientRect(rect => {
    if (rect && !Array.isArray(rect) && typeof rect.width === 'number' && rect.width > 0) plotWidth.value = rect.width
  }).exec()
}
function resized() { void measure() }
onMounted(() => { void measure(); uni.onWindowResize(resized) })
onBeforeUnmount(() => uni.offWindowResize(resized))
</script>
<template>
  <view>
    <view class="item-cost-chart" role="img" aria-label="日均成本随服役日期变化的折线图">
      <view class="item-cost-plot">
        <view v-for="tick in scale.ticks" :key="tick" class="item-cost-grid" :style="{ top: `${(1 - scale.position(tick)) * 144}px` }"><text class="item-cost-axis">{{ axisLabel(tick) }}</text></view>
        <view v-for="(segment, index) in segments" :key="index" class="item-cost-segment" :style="segment" />
        <view v-for="(point, index) in endpoints" :key="index" class="item-cost-point" :style="{ left: `${point.x}px`, top: `${point.y}px` }" />
        <view v-if="points.length" class="item-cost-dates"><text>{{ dateLabel(points[0].date) }}</text><text v-if="points.length > 1">{{ dateLabel(points[points.length - 1].date) }}</text></view>
      </view>
    </view>
    <text v-if="scale.logarithmic" class="item-cost-scale-note">纵轴为非等距刻度，便于查看长期变化</text>
  </view>
</template>
<style scoped>
.item-cost-chart { position:relative; width:100%; height:192px; padding:18px 12px 30px 54px; box-sizing:border-box; }
.item-cost-plot { position:relative; width:100%; height:144px; }
.item-cost-grid { position:absolute; left:0; right:0; height:1px; background:#e9efec; }
.item-cost-axis { position:absolute; right:calc(100% + 7px); top:-7px; color:#858b8b; font-size:10px; white-space:nowrap; }
.item-cost-segment { position:absolute; height:2.5px; background:#49ad9c; transform-origin:0 50%; margin-top:-1.25px; }
.item-cost-point { position:absolute; width:7px; height:7px; border-radius:50%; background:#278879; transform:translate(-50%, -50%); }
.item-cost-dates { position:absolute; top:158px; left:0; right:0; display:flex; justify-content:space-between; color:#858b8b; font-size:10px; white-space:nowrap; }
.item-cost-scale-note { display:block; margin-top:4px; color:#858b8b; font-size:11px; line-height:1.6; }
</style>
