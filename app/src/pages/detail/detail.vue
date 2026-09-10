<script setup lang="ts">
import { computed, ref } from 'vue'
import MoneyDisplay from '../../components/MoneyDisplay.vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { request } from '../../utils/api'
import { cents, inputYuan } from '../../utils/money'
import { backToLedger } from '../../utils/entry'
import { stringId } from '../../utils/id'
import { Transaction, useLedger } from '../../stores/ledger'

type Refund = { id: string; refundNo: string; amountCents: number; createdAt: string }
type Detail = { transaction: Transaction; refundedCents: number; effectiveCents: number; refunds: Refund[] }
const ledger = useLedger()
const detail = ref<Detail | null>(null)
const id = ref('')
const refundAmount = ref('')
const loading = ref(true)
const error = ref('')
const refundError = ref('')
const deleteError = ref('')
let loadSequence = 0
let refundKey = ''
let refundFingerprint = ''
let pageInitialized = false
const refundOpen = ref(false)
const deleteOpen = ref(false)
const deleteTarget = ref<{ type: 'transaction' } | { type: 'refund'; id: string }>({ type: 'transaction' })
const saving = ref(false)
const typeImages = { EXPENSE: 'expense-coin.png', INCOME: 'income-piggy-bank.png', TRANSFER: 'transfer-card.png', REPAYMENT: 'repayment-card.png' }
const accountName = (accountId?: string) => ledger.state.accounts.find(account => account.id === accountId)?.name || '历史账户'
// 首次进入由 onLoad 负责加载；返回详情页时 onShow 重新读取，避免 onShow 早于路由参数导致空 ID 请求。
onShow(() => { if (pageInitialized && id.value && detail.value) void load() })
const label = (type: string) => ({ EXPENSE: '支出', INCOME: '收入', TRANSFER: '转账', REPAYMENT: '还款' }[type] || type)
const accountText = computed(() => {
  const transaction = detail.value?.transaction
  if (!transaction) return '—'
  const name = (accountId?: string) => ledger.state.accounts.find(account => account.id === accountId)?.name || (accountId ? `账户 ${accountId}` : '—')
  if (transaction.type === 'TRANSFER' || transaction.type === 'REPAYMENT') return `${name(transaction.fromAccountId)} → ${name(transaction.toAccountId)}`
  return name(transaction.accountId)
})

async function load() {
  const sequence = ++loadSequence
  loading.value = true
  error.value = ''
  try {
    if (!id.value) throw new Error('账单链接无效，请返回首页重新选择')
    const result = await request<Detail>(`/api/app/transactions/${id.value}`)
    if (sequence === loadSequence) detail.value = result
    // 详情已经成功时，账户刷新失败不应覆盖详情结果；request 已负责提示刷新错误。
    try { await ledger.refresh() } catch { /* 保留已加载的账单详情，等待用户重试刷新。 */ }
  } catch (cause) { if (sequence === loadSequence) error.value = cause instanceof Error ? cause.message : '暂时无法加载账单' }
  finally { if (sequence === loadSequence) loading.value = false }
}
onLoad(query => {
  id.value = stringId(query?.id)
  pageInitialized = true
  if (id.value) void load()
})
function edit() { if (!saving.value && !loading.value) uni.navigateTo({ url: `/pages/entry/entry?id=${id.value}` }) }
function openRefund() {
  refundAmount.value = detail.value ? inputYuan(detail.value.effectiveCents) : ''
  refundError.value = ''
  refundOpen.value = true
}
async function refund() {
  if (!detail.value || !detail.value.effectiveCents || saving.value) return
  saving.value = true
  refundError.value = ''
  try {
    const amount = cents(refundAmount.value || inputYuan(detail.value.effectiveCents))
    if (amount > detail.value.effectiveCents) throw new Error('退款金额不能超过剩余可退款金额')
    const fingerprint = `${id.value}-${amount}`
    if (!refundKey || fingerprint !== refundFingerprint) {
      refundKey = `refund-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
      refundFingerprint = fingerprint
    }
    await request<Refund>(`/api/app/transactions/${id.value}/refunds`, { method: 'POST', data: { amountCents: amount, idempotencyKey: refundKey } })
    refundKey = ''
    refundAmount.value = ''
    refundOpen.value = false
    await load()
    uni.showToast({ title: error.value ? '退款已成功，请重试刷新' : '退款成功', icon: 'none' })
  } catch (cause) { refundError.value = cause instanceof Error ? cause.message : '退款失败，请重试' }
  finally { saving.value = false }
}
const deleteTitle = computed(() => deleteTarget.value.type === 'transaction' ? '删除这笔账单?' : '删除这笔退款?')
const deleteCopy = computed(() => deleteTarget.value.type === 'transaction'
  ? '删除后会同步恢复账户余额，账单及其退款记录也会一并移除。'
  : '删除后会恢复这笔账单的有效金额，确定继续吗？')
function openDelete(target: typeof deleteTarget.value) {
  if (saving.value || loading.value) return
  deleteTarget.value = target
  deleteError.value = ''
  deleteOpen.value = true
}
function applyRefundDeletion(refundId: string) {
  const current = detail.value
  const refund = current?.refunds.find(item => item.id === refundId)
  if (!current || !refund) return
  const refundedCents = Math.max(0, current.refundedCents - refund.amountCents)
  const effectiveCents = current.effectiveCents + refund.amountCents
  detail.value = {
    ...current,
    refundedCents,
    effectiveCents,
    refunds: current.refunds.filter(item => item.id !== refundId),
    transaction: { ...current.transaction, amountCents: effectiveCents, hasRefund: refundedCents > 0 },
  }
}
async function confirmDelete() {
  if (saving.value) return
  saving.value = true
  try {
    if (deleteTarget.value.type === 'transaction') {
      await ledger.deleteTransaction(id.value)
      deleteOpen.value = false
      uni.showToast({ title: '账单已删除', icon: 'success' })
      backToLedger()
      return
    }
    const refundId = deleteTarget.value.id
    await request<void>(`/api/app/transactions/refunds/${refundId}`, { method: 'DELETE' })
    applyRefundDeletion(refundId)
    deleteOpen.value = false
    await load()
    uni.showToast({ title: error.value ? '删除已成功，请重试刷新' : '退款记录已删除', icon: 'none' })
  } catch (cause) {
    deleteError.value = cause instanceof Error ? cause.message : '删除失败，请重试'
  }
  finally { saving.value = false }
}
function removeRefund(refundId: string) { openDelete({ type: 'refund', id: refundId }) }
function remove() { openDelete({ type: 'transaction' }) }
</script>

<template>
  <view class="page detail-page">
    <view class="screen-nav"><button class="back nav-side" aria-label="返回" :disabled="saving" @click="backToLedger">‹</button><text class="page-title">账单详情</text><view class="nav-side" /></view>
    <view v-if="loading && !detail" class="card empty">正在加载账单…</view>
    <view v-else-if="error && !detail" class="card empty">{{ error }}<button class="text-button" @click="load">重试</button></view>
    <template v-else-if="detail">
      <view :class="['detail-hero', detail.transaction.type.toLowerCase()]">
        <view class="detail-hero-heading"><view :class="['type-icon', detail.transaction.type.toLowerCase()]"><image :src="'/static/prototype/' + typeImages[detail.transaction.type]" mode="aspectFit" /></view><text :class="['detail-type-badge', detail.transaction.type.toLowerCase()]">{{ label(detail.transaction.type) }}</text></view>
        <MoneyDisplay class="detail-amount" :prefix="detail.transaction.type === 'EXPENSE' ? '− ' : detail.transaction.type === 'INCOME' ? '＋ ' : ''" :value="detail.effectiveCents" />
        <view :class="['detail-status', { refunded: detail.refundedCents }]"><text class="status-dot" />{{ detail.effectiveCents === 0 ? '已退款' : detail.refundedCents ? '部分退款' : '已完成' }}<MoneyDisplay v-if="detail.refundedCents" prefix=" · " :value="detail.refundedCents" /></view>
        <view class="detail-orbit orbit-one" /><view class="detail-orbit orbit-two" />
      </view>
      <view class="detail-section"><view class="detail-section-heading"><text class="section-kicker">TRANSACTION INFO</text><text class="section-title">账单信息</text></view>
        <view class="detail-info-card">
          <template v-if="detail.transaction.type === 'TRANSFER' || detail.transaction.type === 'REPAYMENT'"><view class="detail-info-row"><text class="field-icon">↗</text><text class="detail-info-label">{{ detail.transaction.type === 'REPAYMENT' ? '还款账户' : '转出账户' }}</text><text class="detail-info-value">{{ accountName(detail.transaction.fromAccountId) }}</text></view><view class="detail-info-row"><text class="field-icon">↘</text><text class="detail-info-label">{{ detail.transaction.type === 'REPAYMENT' ? '信贷账户' : '转入账户' }}</text><text class="detail-info-value">{{ accountName(detail.transaction.toAccountId) }}</text></view></template>
          <view v-else class="detail-info-row"><text class="field-icon">◉</text><text class="detail-info-label">资金账户</text><text class="detail-info-value">{{ accountText }}</text></view>
          <view class="detail-info-row"><text class="field-icon">◷</text><text class="detail-info-label">记账时间</text><text class="detail-info-value">{{ detail.transaction.occurredAt.replace('T', ' ').slice(0, 16) }}</text></view>
          <view class="detail-info-row"><text class="field-icon">⌁</text><text class="detail-info-label">备注</text><text class="detail-info-value">{{ detail.transaction.note || '' }}</text></view>
        </view>
      </view>
      <view class="detail-section"><view class="detail-section-heading"><text class="section-kicker">RECORD</text><text class="section-title">记录来源</text></view><view class="detail-meta-card"><view><text>账单类型</text><text class="meta-value">{{ label(detail.transaction.type) }}</text></view><view><text>记录编号</text><text class="meta-value">{{ detail.transaction.transactionNo }}</text></view></view></view>
      <view v-if="detail.refunds.length" class="detail-section refund-section"><view class="detail-section-heading"><view class="refund-section-heading"><text class="section-kicker">REFUND RECORD</text><text class="section-title">退款记录</text></view><text class="section-meta">{{ detail.refunds.length }} 笔</text></view><view class="refund-record-card"><view class="refund-total-row"><text>累计退款</text><MoneyDisplay class="liability" :value="detail.refundedCents" /></view><view v-for="(record, index) in detail.refunds" :key="record.id" class="refund-record-item"><text class="field-icon">↩</text><view class="account-main"><text class="account-name">第 {{ index + 1 }} 笔退款</text><text class="account-desc">{{ record.createdAt.replace('T', ' ').slice(0, 16) }}</text></view><MoneyDisplay class="liability" :value="record.amountCents" /><button class="refund-delete" :disabled="saving || loading" @click="removeRefund(record.id)">删除</button></view></view></view>
      <view class="detail-actions"><button class="detail-action" :disabled="saving || loading" @click="edit">编辑</button><button v-if="['EXPENSE', 'INCOME'].includes(detail.transaction.type)" class="detail-action refund" :disabled="!detail.effectiveCents || saving || loading" @click="openRefund">{{ detail.effectiveCents ? '退款' : '已退款' }}</button><button class="detail-action delete" :disabled="saving || loading" @click="remove">删除</button></view>
    </template>
    <view v-if="refundOpen && detail" class="refund-backdrop" @click.self="!saving && (refundOpen = false)" @touchmove.stop.prevent>
      <view class="refund-modal" role="dialog" aria-modal="true" aria-label="填写退款金额">
        <view class="refund-handle" />
        <text class="refund-title">填写退款金额</text>
        <view class="refund-copy">退回这笔{{ detail.transaction.type === 'EXPENSE' ? '支出' : '收入' }}，原账单金额为 <MoneyDisplay :value="detail.transaction.originalAmountCents" />，本次最多可退款 <MoneyDisplay :value="detail.effectiveCents" />。</view>
        <view class="refund-field">
          <text class="refund-field-label">本次退款金额</text>
          <view class="refund-input-wrap"><input v-model="refundAmount" type="digit" maxlength="12" :disabled="saving" :placeholder="inputYuan(detail.effectiveCents)" aria-label="本次退款金额（元）" /></view>
        </view>
        <text v-if="refundError" class="refund-error">{{ refundError }}</text>
        <view class="refund-actions"><button class="refund-cancel" :disabled="saving" @click="refundOpen = false">取消</button><button class="refund-confirm" :disabled="saving" @click="refund">{{ saving ? '退款中…' : '确认退款' }}</button></view>
      </view>
    </view>
    <view v-if="deleteOpen" class="asset-create-backdrop" @click.self="!saving && (deleteOpen = false)" @touchmove.stop.prevent>
      <view class="account-delete-modal transaction-delete-modal" role="dialog" aria-modal="true" :aria-label="deleteTitle">
        <view class="asset-create-handle" />
        <text class="account-delete-title">{{ deleteTitle }}</text>
        <text class="account-delete-copy">{{ deleteCopy }}</text>
        <text v-if="deleteError" class="account-delete-error">{{ deleteError }}</text>
        <view class="account-delete-actions"><button class="account-delete-cancel" :disabled="saving" @click="deleteOpen = false">取消</button><button class="account-delete-confirm" :disabled="saving" @click="confirmDelete">{{ saving ? '删除中…' : '确认删除' }}</button></view>
      </view>
    </view>
  </view>
</template>
