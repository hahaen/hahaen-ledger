<script setup lang="ts">
import { ref } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import PageHeader from '../../components/PageHeader.vue'
import BottomNav from '../../components/BottomNav.vue'
import MoneyDisplay from '../../components/MoneyDisplay.vue'
import ItemEditor from '../../components/ItemEditor.vue'
import { itemApi } from '../../utils/api'
import { type ItemOverview, type Item } from '../../utils/items'
import { registerWechatShare } from '../../utils/wechatShare'
registerWechatShare()
const overview = ref<ItemOverview>(), items = ref<Item[]>([]), filter = ref<'ACTIVE' | 'RETIRED' | 'ALL'>('ACTIVE')
const loading = ref(false), error = ref(false), createOpen = ref(false)
let sequence = 0
const retryMore = ref(false)
async function load(more = false) {
  if (more && (loading.value || !overview.value?.hasMore)) return
  const current = ++sequence
  loading.value = true; error.value = false; retryMore.value = more
  const page = more ? (overview.value?.page || 1) + 1 : 1
  try {
    const result = await itemApi.list(filter.value, page)
    if (current !== sequence) return
    overview.value = result
    items.value = more ? [...items.value, ...result.items] : result.items
  } catch { if (current === sequence) error.value = true }
  finally { if (current === sequence) loading.value = false }
}
function select(value: 'ACTIVE' | 'RETIRED' | 'ALL') { filter.value = value; items.value = []; void load() }
function saved() { createOpen.value = false; filter.value = 'ALL'; void load() }
onShow(() => { void load() })
onReachBottom(() => { void load(true) })
</script>
<template>
  <view class="page items-page">
    <PageHeader subtitle="珍惜每一件，让陪伴更长久" />
    <view class="item-summary"><view class="item-summary-title"><text>我的物品</text><view class="item-summary-badge">独立管理</view></view><view class="item-summary-grid"><view><text class="item-caption">物品总资产</text><MoneyDisplay class="item-summary-money" :value="overview?.totalAssetsCents" /><text class="item-caption">在役物品购入总价 · 元</text></view><view><text class="item-caption">总日资产</text><MoneyDisplay class="item-summary-money" :value="overview?.totalDailyCostCents" /><text class="item-caption">在役日均成本之和 · 元/天</text></view></view></view>
    <view class="item-list-heading"><text>物品清单</text><text class="item-caption">{{ overview?.activeCount || 0 }} 件在役 · {{ overview?.retiredCount || 0 }} 件退役</text></view>
    <view class="item-filter"><button v-for="tab in [{ key: 'ACTIVE', label: '正在服役' }, { key: 'RETIRED', label: '已退役' }, { key: 'ALL', label: '全部' }]" :key="tab.key" :class="{ selected: filter === tab.key }" :disabled="loading" @click="select(tab.key as 'ACTIVE' | 'RETIRED' | 'ALL')">{{ tab.label }}</button></view>
    <view class="item-list"><view v-for="item in items" :key="item.id" class="item-row" role="button" :aria-label="`查看${item.name}`" @click="uni.navigateTo({ url: `/pages/item-detail/item-detail?id=${item.id}` })"><view class="item-box-icon"><view /></view><view class="item-row-main"><view class="item-row-title"><text>{{ item.name }}</text><text v-if="item.status === 'RETIRED'" class="item-retired-badge">已退役</text></view><view class="item-row-price"><MoneyDisplay :value="item.priceCents" /><text>元 · 购入价</text></view><view class="item-row-daily"><MoneyDisplay :value="item.dailyCostCents" /><text>元/天{{ item.status === 'RETIRED' ? ' · 最终' : '' }}</text></view></view><view class="item-row-days"><text>{{ item.serviceDays }}</text><text>天{{ item.status === 'RETIRED' ? ' · 已结束' : ' · 陪伴' }}</text></view><text class="item-arrow">›</text></view></view>
    <view v-if="loading" class="card empty">正在加载物品…</view>
    <view v-else-if="error" class="card empty">暂时无法加载物品<button class="text-button" @click="load(retryMore)">重试</button></view>
    <view v-else-if="!items.length" class="card item-empty"><view class="item-box-icon"><view /></view><text>{{ filter === 'RETIRED' ? '还没有退役物品' : '记录物品，看看每一天的陪伴成本' }}</text><button class="text-button" @click="createOpen = true">添加第一件物品</button></view>
    <button v-else-if="overview?.hasMore" class="text-button item-load-more" @click="load(true)">加载更多</button>
    <text v-else class="item-list-end">每一件物品，都有它的陪伴时光</text>
    <button class="fab" aria-label="添加物品" @click="createOpen = true">＋</button><BottomNav active="items" />
    <ItemEditor v-if="createOpen" @close="createOpen = false" @saved="saved" />
  </view>
</template>
