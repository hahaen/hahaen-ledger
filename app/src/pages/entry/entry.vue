<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onBackPress } from '@dcloudio/uni-app'
import CenterModal from '../../components/CenterModal.vue'
import { useLedger, Account } from '../../stores/ledger'
import { request } from '../../utils/api'
import { cents, localDateTime } from '../../utils/money'

type EntryType = 'EXPENSE' | 'INCOME' | 'TRANSFER'
const ledger = useLedger()
const type = ref<EntryType>('EXPENSE')
const amount = ref('')
const note = ref('')
const accountIndex = ref(0)
const toIndex = ref(1)
const dateTime = ref(localDateTime())
const editingId = ref(0)
const saving = ref(false)
const modal = ref<'account' | 'to' | 'date' | 'note' | ''>('')
const formError = ref('')
const draftDate = ref('')
const draftTime = ref('')
const draftNote = ref('')
const initialValue = ref('')
const allowBack = ref(false)
const idempotencyKey = `app-${Date.now()}`
const snapshot = () => JSON.stringify([type.value, amount.value, note.value, accountIndex.value, toIndex.value, dateTime.value])
function back() {
  if (saving.value) return
  if (initialValue.value && initialValue.value !== snapshot()) {
    uni.showModal({ title: '放弃修改？', content: '当前内容尚未保存，确定离开吗？', success: result => { if (result.confirm) { allowBack.value = true; uni.navigateBack() } } })
  } else { allowBack.value = true; uni.navigateBack() }
}
onBackPress(() => { if (allowBack.value) return false; back(); return true })
function openModal(value: typeof modal.value) {
  if (saving.value) return
  draftDate.value = dateTime.value.slice(0, 10)
  draftTime.value = dateTime.value.slice(11, 16)
  draftNote.value = note.value
  modal.value = value
}
function confirmModal() {
  if (modal.value === 'date') dateTime.value = `${draftDate.value}T${draftTime.value}:00`
  if (modal.value === 'note') note.value = draftNote.value
  modal.value = ''
}
function selectAccount(index: number) {
  if (modal.value === 'to') toIndex.value = index
  else accountIndex.value = index
  modal.value = ''
}
const accounts = computed(() => ledger.state.accounts.filter(account => account.kind === 'FUND' && account.status === 'ACTIVE'))
const isEdit = computed(() => editingId.value > 0)
const typeOptions: Array<{ value: EntryType; label: string }> = [{ value: 'EXPENSE', label: '支出' }, { value: 'INCOME', label: '收入' }, { value: 'TRANSFER', label: '转账' }]

function setType(value: EntryType) { type.value = value }
function appendKey(key: string) {
  if (saving.value) return
  formError.value = ''
  if (key === 'C') { amount.value = ''; return }
  if (key === '⌫') { amount.value = amount.value.slice(0, -1); return }
  if (key === '=') { try { calculate() } catch (error) { formError.value = error instanceof Error ? error.message : '计算失败' }; return }
  const operators = new Set(['+', '−', '×', '÷'])
  const last = amount.value.slice(-1)
  if (operators.has(key) && (!amount.value || operators.has(last))) return
  if (key === '.' && amount.value.split(/[+−×÷]/).pop()?.includes('.')) return
  if (key === '.' && (!amount.value || operators.has(last))) amount.value += '0'
  const operand = amount.value.split(/[+−×÷]/).pop() || ''
  if (/\d/.test(key) && operand.includes('.') && operand.split('.')[1].length >= 2) return
  if (amount.value.length >= 32) return
  amount.value += key
}
function calculate() {
  const expression = amount.value.replaceAll('−', '-').replaceAll('×', '*').replaceAll('÷', '/')
  if (!/^\d+(\.\d{1,2})?([+\-*/]\d+(\.\d{1,2})?)*$/.test(expression)) throw new Error('算式有误，请检查后重试')
  const values: number[] = []; const operators: string[] = []; let number = ''
  const apply = () => { const right = values.pop() || 0; const left = values.pop() || 0; const operator = operators.pop() || '+'; if (operator === '/' && right === 0) throw new Error('除数不能为 0，请重新输入'); values.push(operator === '+' ? left + right : operator === '-' ? left - right : operator === '*' ? left * right : left / right) }
  for (const character of expression) { if (/\d|\./.test(character)) number += character; else { values.push(Number(number)); number = ''; while (operators.length && ('*/'.includes(operators.at(-1) || '') || '+-'.includes(character) && '+-'.includes(operators.at(-1) || ''))) apply(); operators.push(character) } }
  values.push(Number(number)); while (operators.length) apply()
  const result = values[0]
  if (!Number.isFinite(result) || result <= 0 || result > 999999999.99) throw new Error('金额必须在 ¥0.01～¥999,999,999.99 之间')
  amount.value = result.toFixed(2).replace(/\.00$/, '').replace(/(\.\d)0$/, '$1')
}
async function loadForEdit(id: number) {
  await ledger.refresh()
  const result = await request<{ transaction: { type: EntryType; originalAmountCents: number; accountId?: number; fromAccountId?: number; toAccountId?: number; occurredAt: string; note?: string } }>(`/api/app/transactions/${id}`)
  const transaction = result.transaction
  if (!['EXPENSE', 'INCOME', 'TRANSFER'].includes(transaction.type)) throw new Error('还款流水不能从此处编辑')
  editingId.value = id; type.value = transaction.type; amount.value = (transaction.originalAmountCents / 100).toFixed(2); note.value = transaction.note || ''; dateTime.value = transaction.occurredAt.slice(0, 16)
  const from = transaction.fromAccountId ?? transaction.accountId
  accountIndex.value = Math.max(0, accounts.value.findIndex(account => account.id === from))
  toIndex.value = Math.max(0, accounts.value.findIndex(account => account.id === transaction.toAccountId))
}
onLoad(async query => {
  const routeId = Number(query?.id || 0)
  try {
    if (routeId) await loadForEdit(routeId)
    else {
      if (!ledger.state.accounts.length) await ledger.refresh()
      const lastId = uni.getStorageSync('last-entry-account')
      accountIndex.value = Math.max(0, accounts.value.findIndex(account => account.id === lastId))
    }
    initialValue.value = snapshot()
  } catch (error) { uni.showToast({ title: error instanceof Error ? error.message : '加载失败，请重试', icon: 'none' }) }
})
async function save() {
  if (saving.value) return
  saving.value = true
  try {
    formError.value = ''
    if (!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2})?$/.test(dateTime.value) || !Number.isFinite(new Date(dateTime.value).getTime())) throw new Error('请选择有效日期和时间')
    if (!amount.value) throw new Error('请输入大于 0 的金额')
    if (/[+−×÷]/.test(amount.value)) calculate()
    const payload: Record<string, unknown> = { type: type.value, amountCents: cents(amount.value), occurredAt: dateTime.value, note: note.value.trim() || undefined, idempotencyKey: isEdit.value ? undefined : idempotencyKey }
    if (type.value === 'TRANSFER') {
      const from = accounts.value[accountIndex.value]; const to = accounts.value[toIndex.value]
      if (!from || !to) throw new Error('请先创建两个资金账户')
      if (from.id === to.id) throw new Error('转出账户和转入账户不能相同')
      payload.fromAccountId = from.id; payload.toAccountId = to.id
    } else {
      const account = accounts.value[accountIndex.value]
      if (!account) throw new Error('请先创建资金账户')
      payload.accountId = account.id
    }
    if (isEdit.value) await ledger.updateTransaction(editingId.value, payload); else await ledger.createTransaction(payload)
    uni.setStorageSync('last-entry-account', accounts.value[accountIndex.value].id)
    allowBack.value = true
    uni.showToast({ title: isEdit.value ? '账单已更新，余额已刷新' : '记账成功，余额已刷新', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 400)
  } catch (error) { uni.showToast({ title: error instanceof Error ? error.message : '保存失败，请重试', icon: 'none' }) } finally { saving.value = false }
}
</script>

<template>
  <view class="page entry-page">
    <view class="entry-content">
      <view class="screen-nav"><button class="back nav-side" aria-label="返回" @click="back">‹</button><text class="page-title">{{ isEdit ? '编辑记账' : '新增记账' }}</text><view class="nav-side" /></view>
      <view class="entry-type"><button v-for="option in typeOptions" :key="option.value" :disabled="saving" :class="{ active: type === option.value, 'income-active': type === 'INCOME' && type === option.value }" @click="setType(option.value)">{{ option.label }}</button></view>
      <view class="amount-panel"><text class="amount-label">{{ type === 'TRANSFER' ? '转账金额' : '记账金额' }}</text><view class="amount-display"><text class="currency">¥</text><text :class="['amount-value', { 'amount-placeholder': !amount, 'long-amount': amount.length > 10 }]">{{ amount || '输入金额' }}</text></view><button class="calculation-line" aria-label="计算结果" @click="appendKey('=')">支持 + − × ÷ 连续计算<text v-if="/[+−×÷]/.test(amount)"> · 点此计算</text></button><view v-if="formError" class="inline-error">{{ formError }}</view></view>
      <view class="fields-card">
        <button class="field-row" @click="openModal('account')"><text class="field-icon">{{ type === 'TRANSFER' ? '↗' : '◉' }}</text><text class="field-label">{{ type === 'TRANSFER' ? '转出账户' : '资金账户' }}</text><text class="field-value">{{ accounts[accountIndex]?.name || '请选择' }}</text><text class="arrow">›</text></button>
        <button v-if="type === 'TRANSFER'" class="field-row" @click="openModal('to')"><text class="field-icon">↘</text><text class="field-label">转入账户</text><text class="field-value">{{ accounts[toIndex]?.name || '请选择' }}</text><text class="arrow">›</text></button>
        <button class="field-row" @click="openModal('date')"><text class="field-icon">◷</text><text class="field-label">日期与时间</text><text class="field-value">{{ dateTime.replace('T', ' ').slice(0, 16) }}</text><text class="arrow">›</text></button>
        <button class="field-row" @click="openModal('note')"><text class="field-icon">⌁</text><text class="field-label">备注</text><text :class="['field-value', { placeholder: !note }]">{{ note || '写点说明...' }}</text></button>
      </view>
    </view>
    <view class="keypad" aria-label="金额键盘">
      <view v-for="key in ['1', '2', '3', '⌫', '4', '5', '6', '+−', '7', '8', '9', '×÷', 'C', '0', '.', 'confirm']" :key="key" :class="['keypad-cell', { 'keypad-pair': key === '+−' || key === '×÷' }]">
        <template v-if="key === '+−' || key === '×÷'"><button v-for="operator in key.split('')" :key="operator" class="key" :disabled="saving" @click="appendKey(operator)">{{ operator }}</button></template>
        <button v-else-if="key === 'confirm'" :class="['key', 'key-cancel', { 'key-confirm': !!amount }]" :disabled="saving" @click="amount ? save() : back()">{{ saving ? '保存中' : amount ? '确定' : '取消' }}</button>
        <button v-else :class="['key', { 'key-text': key === 'C', 'key-backspace': key === '⌫' }]" :aria-label="key === 'C' ? '清除金额' : key === '⌫' ? '退格' : key" :disabled="saving" @click="appendKey(key)">{{ key === 'C' ? '再记' : key }}</button>
      </view>
    </view>
    <CenterModal v-if="modal" :title="modal === 'note' ? '添加备注' : modal === 'date' ? '日期与时间' : modal === 'to' ? '选择转入账户' : '选择账户'" @close="modal = ''">
      <template v-if="modal === 'account' || modal === 'to'"><view v-if="!accounts.length" class="list-empty">暂无资金账户<button class="text-button" @click="modal = ''; uni.navigateTo({ url: '/pages/account/account?kind=FUND' })">添加账户</button></view><view class="sheet-options"><button v-for="(account, index) in accounts" :key="account.id" :class="['sheet-option', { selected: index === (modal === 'to' ? toIndex : accountIndex) }]" @click="selectAccount(index)"><image class="account-icon" src="/static/prototype/funds-account.png" />{{ account.name }}</button></view></template>
      <template v-else-if="modal === 'date'"><view class="field"><label>日期</label><picker mode="date" :value="draftDate" @change="draftDate = $event.detail.value">{{ draftDate }}</picker></view><view class="field"><label>时间</label><picker mode="time" :value="draftTime" @change="draftTime = $event.detail.value">{{ draftTime }}</picker></view></template>
      <view v-else class="field"><textarea v-model="draftNote" maxlength="100" placeholder="写点说明..." :show-confirm-bar="false" /><text class="section-meta">{{ draftNote.length }} / 100</text></view>
      <template v-if="modal === 'date' || modal === 'note'" #actions><view class="sheet-actions"><button class="secondary-btn" @click="modal = ''">取消</button><button class="primary-btn" @click="confirmModal">确定</button></view></template>
    </CenterModal>
  </view>
</template>
