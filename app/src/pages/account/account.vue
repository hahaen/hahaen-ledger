<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useLedger } from '../../stores/ledger'
import { request } from '../../utils/api'
import { yuan } from '../../utils/money'

const ledger = useLedger()
const account = ref<any>()
const records = ref<any[]>([])
const id = ref(0)
const kind = ref('FUND')
const formName = ref('')
const balance = ref('0')
const limit = ref('0')
const included = ref(true)
const saving = ref(false)
const isCreate = computed(() => !id.value)

onLoad((query) => { id.value = Number(query?.id || 0); kind.value = String(query?.kind || 'FUND'); if (id.value) load() })
onShow(() => { if (id.value) load() })

async function load() {
  account.value = await request(`/api/app/accounts/${id.value}`)
  records.value = await request(`/api/app/transactions?accountId=${id.value}`)
  formName.value = account.value.name
  balance.value = (account.value.balanceCents / 100).toFixed(2)
  limit.value = (account.value.creditLimitCents / 100).toFixed(2)
  included.value = account.value.includedInNetAsset
}

async function save() {
  if (saving.value) return
  saving.value = true
  try {
    const payload = { name: formName.value.trim(), kind: account.value?.kind || kind.value, balanceCents: Math.round(Number(balance.value) * 100), creditLimitCents: Math.round(Number(limit.value || 0) * 100), includedInNetAsset: included.value }
    if (!payload.name) throw new Error('请输入账户名称')
    if (isCreate.value) await ledger.createAccount(payload)
    else await request(`/api/app/accounts/${id.value}`, { method: 'PUT', data: payload })
    uni.showToast({ title: '账户已保存', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 350)
  } catch (e: any) {
    uni.showToast({ title: e.message || '保存失败，请重试', icon: 'none' })
  } finally { saving.value = false }
}

function onIncluded(event: any) { included.value = Boolean(event.detail.value) }
async function repay() {
  const fund = ledger.state.accounts.find(a => a.kind === 'FUND' && a.status === 'ACTIVE')
  if (!fund) return uni.showToast({ title: '请先创建资金账户', icon: 'none' })
  if (!account.value?.balanceCents) return
  uni.showModal({ title: '全额还款', content: `从 ${fund.name} 还款 ${yuan(account.value.balanceCents)}？`, success: async (result) => { if (result.confirm) { await request(`/api/app/accounts/${id.value}/repayments`, { method: 'POST', data: { fundAccountId: fund.id, amountCents: account.value.balanceCents, idempotencyKey: `repay-${id.value}-${Date.now()}` } }); uni.showToast({ title: '还款成功', icon: 'success' }); await load() } } })
}
async function disable() { uni.showModal({ title: '停用账户', content: '历史流水会保留，新账单不能再选择，确定停用吗？', success: async (result) => { if (result.confirm) { await request(`/api/app/accounts/${id.value}`, { method: 'DELETE' }); uni.navigateBack() } } }) }
</script>

<template>
  <view class="page">
    <view class="topbar"><button class="back" aria-label="返回" @click="uni.navigateBack()">‹</button><text class="section-title" style="margin:0">{{isCreate?'新增账户':'账户详情'}}</text><view style="width:40px"/></view>
    <view class="card"><view class="field"><label>账户名称</label><input v-model="formName" maxlength="20" placeholder="如：微信、银行卡"/></view><view class="field"><label>{{kind==='FUND'?'当前余额（元）':'当前欠款（元）'}}</label><input v-model="balance" type="digit"/></view><view v-if="kind==='CREDIT'" class="field"><label>总额度（元）</label><input v-model="limit" type="digit"/></view><view class="account-row field"><text>计入净资产</text><switch :checked="included" @change="onIncluded" color="#49AD9C"/></view></view>
    <button class="primary-btn" :disabled="saving" @click="save">{{saving?'保存中…':'保存账户'}}</button>
    <button v-if="!isCreate && kind==='CREDIT'" class="secondary-btn" style="margin-top:12px" @click="repay">还款</button>
    <button v-if="!isCreate" class="danger-btn" style="margin-top:12px" @click="disable">停用账户</button>
    <text v-if="!isCreate" class="section-title" style="display:block">账户流水</text>
    <view v-if="!isCreate && !records.length" class="card empty">这个账户的收支会在这里呈现</view>
    <button v-for="record in records" :key="record.id" class="transaction" @click="uni.navigateTo({url:`/pages/detail/detail?id=${record.id}`})"><text class="transaction-icon">{{record.type==='INCOME'?'收':record.type==='EXPENSE'?'支':record.type==='TRANSFER'?'转':'还'}}</text><view class="transaction-copy"><text class="transaction-title">{{record.type}}</text><text class="transaction-note">{{record.occurredAt.replace('T',' ')}} · {{record.note || '无备注'}}</text></view><text class="transaction-amount">{{yuan(record.amountCents)}}</text><text class="arrow">›</text></button>
  </view>
</template>
