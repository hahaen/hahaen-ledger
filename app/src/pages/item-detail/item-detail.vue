<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import NativeNavigation from '../../components/NativeNavigation.vue'
import MoneyDisplay from '../../components/MoneyDisplay.vue'
import ItemEditor from '../../components/ItemEditor.vue'
import ItemProfileEditor from '../../components/ItemProfileEditor.vue'
import ItemCostChart from '../../components/ItemCostChart.vue'
import CenterModal from '../../components/CenterModal.vue'
import { itemApi } from '../../utils/api'
import { itemSubmission, type ItemDetail } from '../../utils/items'
const detail = ref<ItemDetail>(), loading = ref(false), error = ref(false)
const retireOpen = ref(false), editOpen = ref(false), deleteOpen = ref(false), deleting = ref(false), deleteError = ref('')
const reactivateOpen = ref(false), reactivating = ref(false), reactivateError = ref('')
let id = ''
const submission = itemSubmission()
let reactivateSubmission = itemSubmission()
function back() { uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/items/items' }) }) }
async function load() {
  if (!id) return
  loading.value = true; error.value = false
  try { detail.value = await itemApi.detail(id) }
  catch { error.value = true }
  finally { loading.value = false }
}
async function retired() { retireOpen.value = false; await load() }
async function reactivate() {
  if (reactivating.value) return
  reactivating.value = true; reactivateError.value = ''
  try {
    const item = await itemApi.reactivate(id, reactivateSubmission({ id, action: 'reactivate' }).idempotencyKey)
    reactivateSubmission = itemSubmission()
    if (detail.value) detail.value.item = item
    reactivateOpen.value = false
    uni.showToast({ title: '已重新服役', icon: 'success' })
    await load()
  } catch (cause) { reactivateError.value = cause instanceof Error ? cause.message : '重新服役失败，请重试' }
  finally { reactivating.value = false }
}
async function remove() {
  if (deleting.value) return
  deleting.value = true; deleteError.value = ''
  try {
    await itemApi.remove(id, submission({ id, action: 'delete' }).idempotencyKey)
    uni.showToast({ title: '物品已删除', icon: 'success' }); back()
  } catch (cause) { deleteError.value = cause instanceof Error ? cause.message : '删除失败，请重试' }
  finally { deleting.value = false }
}
onShow(() => { if (/^\d+$/.test(id) && !loading.value) void load() })
onLoad(options => { id = options?.id || ''; if (!/^\d+$/.test(id)) error.value = true; else void load() })
</script>
<template>
  <view class="page item-detail-page"><NativeNavigation variant="screen" title="物品详情" compact @back="back" />
    <view v-if="loading" class="card empty">正在加载物品…</view><view v-else-if="error" class="card empty">物品暂时无法查看<button class="text-button" @click="load">重试</button></view>
    <template v-else-if="detail">
      <view class="item-detail-hero"><view class="item-detail-heading"><view class="item-box-icon"><view /></view><view><text class="item-detail-name">{{ detail.item.name }}</text><text :class="['item-status', { retired: detail.item.status === 'RETIRED' }]">{{ detail.item.status === 'ACTIVE' ? '正在服役' : '已退役' }}</text></view></view><text class="item-caption">{{ detail.item.status === 'ACTIVE' ? '当前日均成本' : '最终日均成本' }}</text><view class="item-cost-value"><MoneyDisplay :value="detail.item.dailyCostCents" /><text>元 / 天</text></view><text class="item-caption">{{ detail.item.status === 'ACTIVE' ? '已陪伴' : '共服役' }} {{ detail.item.serviceDays }} 天{{ detail.item.status === 'ACTIVE' ? '，成本随陪伴逐日摊薄' : '，服役天数已冻结' }}</text></view>
      <view class="card item-information"><view><text>购买价格</text><MoneyDisplay :value="detail.item.priceCents" /></view><view><text>购买日期</text><text>{{ detail.item.purchasedOn }}</text></view><template v-if="detail.item.status === 'RETIRED'"><view><text>退役日期</text><text>{{ detail.item.retiredOn }}</text></view><view><text>二手出售价格</text><MoneyDisplay :value="detail.item.resaleCents" /></view><view><text>实际净成本</text><MoneyDisplay :value="detail.item.netCostCents" /></view></template></view>
      <view class="card item-chart-card"><view class="item-chart-title"><text>日均成本走势</text><text class="item-caption">元 / 天</text></view><ItemCostChart :points="detail.costHistory" /><text class="item-chart-note">{{ detail.item.status === 'ACTIVE' ? '购入价 ÷ 服役天数。购买当天算第1天。' : '退役终点计入二手售价，最终成本为（购入价 − 售价）÷ 服役天数。' }}{{ detail.item.netCostCents < 0 ? '负值表示出售回收金额超过购入价。' : '' }}</text></view>
      <view class="item-independence">物品独立管理，购入和出售均不会自动生成账单。</view>
      <view class="detail-actions item-detail-actions"><button class="item-retire-button" @click="editOpen = true">编辑</button><button v-if="detail.item.status === 'ACTIVE'" class="item-retire-button" @click="retireOpen = true">退役</button><button v-else class="item-retire-button" @click="reactivateError = ''; reactivateOpen = true">重新服役</button><button class="item-delete-button" :disabled="deleting" @click="deleteOpen = true">删除</button></view>
      <ItemEditor v-if="retireOpen" :item="detail.item" @close="retireOpen = false" @saved="retired" />
      <ItemProfileEditor v-if="editOpen" :item="detail.item" @close="editOpen = false" @saved="editOpen = false; load()" />
      <CenterModal v-if="reactivateOpen" title="重新服役" @close="!reactivating && (reactivateOpen = false)"><view class="item-delete-copy">确定让「{{ detail.item.name }}」重新服役？原退役日期和二手出售价格会清空，日均成本将按购买日期至今天重新计算。</view><text v-if="reactivateError" class="item-delete-error">{{ reactivateError }}</text><template #actions><view class="item-delete-actions"><button :disabled="reactivating" @click="reactivateOpen = false">取消</button><button class="item-retire-button" :disabled="reactivating" :loading="reactivating" @click="reactivate">确认服役</button></view></template></CenterModal>
      <CenterModal v-if="deleteOpen" title="删除物品" @close="!deleting && (deleteOpen = false)"><view class="item-delete-copy">确定删除「{{ detail.item.name }}」？删除后将从物品清单和汇总中移除。</view><text v-if="deleteError" class="item-delete-error">{{ deleteError }}</text><template #actions><view class="item-delete-actions"><button :disabled="deleting" @click="deleteOpen = false">取消</button><button class="danger" :disabled="deleting" :loading="deleting" @click="remove">确认删除</button></view></template></CenterModal>
    </template>
  </view>
</template>
