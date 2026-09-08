<script setup lang="ts">
import { computed, ref } from 'vue'
import CenterModal from '../../components/CenterModal.vue'
import MoneyDisplay from '../../components/MoneyDisplay.vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { request } from '../../utils/api'
import { cents, formatYuan } from '../../utils/money'
import { stringId } from '../../utils/id'
import { Transaction, useLedger } from '../../stores/ledger'

type Refund = { id: string; refundNo: string; amountCents: number; createdAt: string }
type Detail = { transaction: Transaction; refundedCents: number; effectiveCents: number; refunds: Refund[] }
const ledger = useLedger()
const detail = ref<Detail | null>(null)
const id = ref('')
const refundAmount = ref('')
const loading = ref(true)
const error = ref(false)
const refundOpen = ref(false)
const saving = ref(false)
const typeImages = { EXPENSE: 'expense-coin.png', INCOME: 'income-piggy-bank.png', TRANSFER: 'transfer-card.png', REPAYMENT: 'repayment-card.png' }
const accountName = (accountId?: string) => ledger.state.accounts.find(account => account.id === accountId)?.name || '历史账户'
onShow(() => { if (id.value) void load() })
const label = (type: string) => ({ EXPENSE: '支出', INCOME: '收入', TRANSFER: '转账', REPAYMENT: '还款' }[type] || type)
const accountText = computed(() => {
  const transaction = detail.value?.transaction
  if (!transaction) return '—'
  const name = (accountId?: string) => ledger.state.accounts.find(account => account.id === accountId)?.name || (accountId ? `账户 ${accountId}` : '—')
  if (transaction.type === 'TRANSFER' || transaction.type === 'REPAYMENT') return `${name(transaction.fromAccountId)} → ${name(transaction.toAccountId)}`
  return name(transaction.accountId)
})

async function load() {
  loading.value = true
  error.value = false
  try { detail.value = await request<Detail>(`/api/app/transactions/${id.value}`) } catch { error.value = true } finally { loading.value = false }
}
onLoad(async query => { id.value = stringId(query?.id); if (id.value) await load() })
function edit() { uni.navigateTo({ url: `/pages/entry/entry?id=${id.value}` }) }
async function refund() {
  if (!detail.value || !detail.value.effectiveCents || saving.value) return
  saving.value = true
  try {
    const amount = cents(refundAmount.value || formatYuan(detail.value.effectiveCents))
    if (amount > detail.value.effectiveCents) throw new Error('退款金额不能超过剩余可退款金额')
    await request<Refund>(`/api/app/transactions/${id.value}/refunds`, { method: 'POST', data: { amountCents: amount, idempotencyKey: `refund-${id.value}-${Date.now()}` } })
    refundAmount.value = ''
    refundOpen.value = false
    await Promise.all([load(), ledger.refresh()])
    uni.showToast({ title: '退款成功，余额已刷新', icon: 'success' })
  } catch (error) { uni.showToast({ title: error instanceof Error ? error.message : '退款失败，请重试', icon: 'none' }) } finally { saving.value = false }
}
function removeRefund(refundId: string) {
  uni.showModal({ title: '删除退款', content: '删除后会恢复这笔账单的有效金额，确定继续吗？', success: async result => {
    if (!result.confirm) return
    try { await request<void>(`/api/app/transactions/refunds/${refundId}`, { method: 'DELETE' }); await Promise.all([load(), ledger.refresh()]) } catch { /* request 已显示失败原因 */ }
  } })
}
function remove() {
  uni.showModal({ title: '删除账单', content: '删除后会同步恢复账户余额，确定继续吗？', success: async result => {
    if (!result.confirm) return
    try { await ledger.deleteTransaction(id.value); uni.navigateBack() } catch { /* request 已显示失败原因 */ }
  } })
}
</script>

<template>
  <view class="page detail-page">
    <view class="screen-nav"><button class="back nav-side" aria-label="返回" @click="uni.navigateBack()">‹</button><text class="page-title">账单详情</text><view class="nav-side" /></view>
    <view v-if="loading && !detail" class="card empty">正在加载账单…</view>
    <view v-else-if="error" class="card empty">暂时无法加载账单<button class="text-button" @click="load">重试</button></view>
    <template v-else-if="detail">
      <view :class="['detail-hero', detail.transaction.type.toLowerCase()]">
        <view class="detail-hero-heading"><view :class="['type-icon', detail.transaction.type.toLowerCase()]"><image :src="'/static/prototype/' + typeImages[detail.transaction.type]" mode="aspectFit" /></view><text :class="['detail-type-badge', detail.transaction.type.toLowerCase()]">{{ label(detail.transaction.type) }}</text></view>
        <MoneyDisplay class="detail-amount" :prefix="detail.transaction.type === 'EXPENSE' ? '− ' : detail.transaction.type === 'INCOME' ? '＋ ' : ''" :value="detail.transaction.originalAmountCents" />
        <view :class="['detail-status', { refunded: detail.refundedCents }]"><text class="status-dot" />{{ detail.effectiveCents === 0 ? '已退款' : detail.refundedCents ? '部分退款' : '已完成' }}<MoneyDisplay v-if="detail.refundedCents" prefix=" · " :value="detail.refundedCents" /></view>
        <view class="detail-orbit orbit-one" /><view class="detail-orbit orbit-two" />
      </view>
      <view class="detail-section"><view class="detail-section-heading"><text class="section-kicker">TRANSACTION INFO</text><text class="section-title">账单信息</text></view>
        <view class="detail-info-card">
          <template v-if="detail.transaction.type === 'TRANSFER' || detail.transaction.type === 'REPAYMENT'"><view class="detail-info-row"><text class="field-icon">↗</text><text class="detail-info-label">{{ detail.transaction.type === 'REPAYMENT' ? '还款账户' : '转出账户' }}</text><text class="detail-info-value">{{ accountName(detail.transaction.fromAccountId) }}</text></view><view class="detail-info-row"><text class="field-icon">↘</text><text class="detail-info-label">{{ detail.transaction.type === 'REPAYMENT' ? '信贷账户' : '转入账户' }}</text><text class="detail-info-value">{{ accountName(detail.transaction.toAccountId) }}</text></view></template>
          <view v-else class="detail-info-row"><text class="field-icon">◉</text><text class="detail-info-label">资金账户</text><text class="detail-info-value">{{ accountText }}</text></view>
          <view class="detail-info-row"><text class="field-icon">◷</text><text class="detail-info-label">记账时间</text><text class="detail-info-value">{{ detail.transaction.occurredAt.replace('T', ' ').slice(0, 16) }}</text></view>
          <view class="detail-info-row"><text class="field-icon">⌁</text><text class="detail-info-label">备注</text><text class="detail-info-value">{{ detail.transaction.note || '未填写' }}</text></view>
        </view>
      </view>
      <view class="detail-section"><view class="detail-section-heading"><text class="section-kicker">RECORD</text><text class="section-title">记录来源</text></view><view class="detail-meta-card"><view><text>账单类型</text><text class="meta-value">{{ label(detail.transaction.type) }}</text></view><view><text>记录编号</text><text class="meta-value">{{ detail.transaction.transactionNo }}</text></view></view></view>
      <view v-if="detail.refunds.length" class="detail-section"><view class="detail-section-heading"><text class="section-title">退款记录</text><text class="section-meta">{{ detail.refunds.length }} 笔</text></view><view class="detail-info-card"><view class="detail-info-row"><text class="detail-info-label">累计退款</text><MoneyDisplay class="liability" :value="detail.refundedCents" /></view><view v-for="(record, index) in detail.refunds" :key="record.id" class="refund-record-item"><text class="field-icon">↩</text><view class="account-main"><text class="account-name">第 {{ index + 1 }} 笔退款</text><text class="account-desc">{{ record.createdAt.replace('T', ' ').slice(0, 16) }}</text></view><MoneyDisplay class="liability" :value="record.amountCents" /><button class="refund-delete" @click="removeRefund(record.id)">删除</button></view></view></view>
      <view class="detail-actions"><button v-if="detail.transaction.type !== 'REPAYMENT'" class="detail-action" @click="edit">编辑</button><button v-if="['EXPENSE', 'INCOME'].includes(detail.transaction.type)" class="detail-action refund" :disabled="!detail.effectiveCents || saving" @click="refundOpen = true">{{ detail.effectiveCents ? '退款' : '已退款' }}</button><button class="detail-action delete" @click="remove">删除</button></view>
    </template>
    <CenterModal v-if="refundOpen && detail" title="填写退款金额" @close="!saving && (refundOpen = false)"><view class="confirm-copy">原账单金额 <MoneyDisplay :value="detail.transaction.originalAmountCents" />，本次最多可退款 <MoneyDisplay :value="detail.effectiveCents" />。</view><view class="field"><label>本次退款金额（元）</label><input v-model="refundAmount" type="digit" :placeholder="formatYuan(detail.effectiveCents)" :disabled="saving" /></view><template #actions><view class="sheet-actions"><button class="secondary-btn" :disabled="saving" @click="refundOpen = false">取消</button><button class="primary-btn" :disabled="saving" @click="refund">{{ saving ? '退款中…' : '确认退款' }}</button></view></template></CenterModal>
  </view>
</template>
