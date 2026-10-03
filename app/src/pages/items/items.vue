<script lang="ts">
// #ifdef MP-WEIXIN
import { createWechatShareMessage } from '../../utils/wechatShare'
// #endif

export default {
  // #ifdef MP-WEIXIN
  onShareAppMessage: createWechatShareMessage,
  // #endif
}
</script>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import PageHeader from '../../components/PageHeader.vue'
import BottomNav from '../../components/BottomNav.vue'
import MoneyDisplay from '../../components/MoneyDisplay.vue'
import ItemEditor from '../../components/ItemEditor.vue'
import { useLedger } from '../../stores/ledger'
import { itemApi } from '../../utils/api'
import { type ItemOverview, type Item } from '../../utils/items'
const overview = ref<ItemOverview>(), items = ref<Item[]>([]), filter = ref<'ACTIVE' | 'RETIRED' | 'ALL'>('ALL')
const loading = ref(false), error = ref(false), createOpen = ref(false)
const ledger = useLedger()
const loadedFilter = ref('')
const ready = computed(() => Boolean(overview.value) && loadedFilter.value === filter.value)
let sequence = 0
watch(() => ledger.state.token, () => {
  ++sequence
  overview.value = undefined
  items.value = []
  loadedFilter.value = ''
  loading.value = error.value = false
}, { flush: 'sync' })
const retryMore = ref(false)
async function load(more = false) {
  if (more && (loading.value || !overview.value?.hasMore)) return
  const current = ++sequence
  if (!ready.value) items.value = []
  const requestedFilter = filter.value
  loading.value = true; error.value = false; retryMore.value = more
  const page = more ? (overview.value?.page || 1) + 1 : 1
  try {
    const result = await itemApi.list(requestedFilter, page)
    if (current !== sequence) return
    loadedFilter.value = requestedFilter
    overview.value = result
    items.value = more ? [...items.value, ...result.items] : result.items
  } catch {
    if (current === sequence) {
      error.value = true
      if (ready.value && !more) uni.showToast({ title: '刷新物品失败，已保留原数据，请重试', icon: 'none' })
    }
  }
  finally { if (current === sequence) loading.value = false }
}
function select(value: 'ACTIVE' | 'RETIRED' | 'ALL') { if (filter.value === value) return; filter.value = value; void load() }
function saved() { createOpen.value = false; filter.value = 'ALL'; void load() }
onShow(() => { void load() })
onReachBottom(() => { void load(true) })
</script>
<template>
  <view class="page items-page">
    <PageHeader subtitle="珍惜每一件，让陪伴更长久" />
    <view class="item-summary"><view class="item-summary-title"><text>我的物品</text><view class="item-summary-badge">独立管理</view></view><view class="item-summary-grid"><view><text class="item-caption">物品总资产</text><MoneyDisplay class="item-summary-money" :value="overview?.totalAssetsCents" /><text class="item-caption">在役物品购入总价 · 元</text></view><view><text class="item-caption">总日资产</text><MoneyDisplay class="item-summary-money" :value="overview?.totalDailyCostCents" /><text class="item-caption">在役日均成本之和 · 元/天</text></view></view></view>
    <view class="item-list-heading"><text>物品清单</text><text class="item-caption">{{ overview?.activeCount || 0 }} 件在役 · {{ overview?.retiredCount || 0 }} 件退役</text></view>
    <view class="item-filter"><button v-for="tab in [{ key: 'ALL', label: '全部' }, { key: 'ACTIVE', label: '正在服役' }, { key: 'RETIRED', label: '已退役' }]" :key="tab.key" :class="{ selected: filter === tab.key }" :disabled="loading && !ready" @click="select(tab.key as 'ACTIVE' | 'RETIRED' | 'ALL')">{{ tab.label }}</button></view>
    <view class="item-list"><view v-for="item in items" :key="item.id" class="item-row" :class="{ 'item-row-retired': item.status === 'RETIRED' }" role="button" :aria-label="`查看${item.name}，购入时间${item.purchasedOn}`" @click="uni.navigateTo({ url: `/pages/item-detail/item-detail?id=${item.id}` })"><view class="item-box-icon"><view /></view><view class="item-row-main"><view class="item-row-title"><text>{{ item.name }}</text><text v-if="item.status === 'RETIRED'" class="item-retired-badge">已退役</text></view><view class="item-row-costs"><view class="item-row-price"><MoneyDisplay :value="item.netCostCents" /><text>元</text></view><text class="item-row-separator">·</text><view class="item-row-daily"><MoneyDisplay :value="item.dailyCostCents" /><text>元/天{{ item.status === 'RETIRED' ? ' · 最终' : '' }}</text></view></view><view class="item-row-purchased"><text>购入时间 {{ item.purchasedOn }}</text></view></view><view class="item-row-days"><text>{{ item.serviceDays }}</text><text>天</text></view><text class="item-arrow">›</text></view></view>
    <view v-if="loading && (!ready || retryMore)" class="card empty">正在加载物品…</view>
    <view v-else-if="error && (!ready || retryMore)" class="card empty">暂时无法加载物品<button class="text-button" @click="load(retryMore)">重试</button></view>
    <view v-else-if="!items.length" class="card item-empty"><view class="item-box-icon"><view /></view><text>{{ filter === 'RETIRED' ? '还没有退役物品' : '记录物品，看看每一天的陪伴成本' }}</text><button class="text-button" @click="createOpen = true">添加第一件物品</button></view>
    <button v-else-if="overview?.hasMore" class="text-button item-load-more" @click="load(true)">加载更多</button>
    <text v-else class="item-list-end">每一件物品，都有它的陪伴时光</text>
    <button class="fab" aria-label="添加物品" @click="createOpen = true">＋</button><BottomNav active="items" />
    <ItemEditor v-if="createOpen" @close="createOpen = false" @saved="saved" />
  </view>
</template>
