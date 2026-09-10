<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onBackPress } from '@dcloudio/uni-app'
import EntryDateTimePicker from '../../components/EntryDateTimePicker.vue'
import MoneyDisplay from '../../components/MoneyDisplay.vue'
import { useLedger, TransactionPayload } from '../../stores/ledger'
import { request } from '../../utils/api'
import { cents, inputYuan, localDateTime } from '../../utils/money'
import { calculateAmount, validLocalDateTime, backToLedger } from '../../utils/entry'
import { stringId } from '../../utils/id'

type EntryType = 'EXPENSE' | 'INCOME' | 'TRANSFER' | 'REPAYMENT'
const ledger = useLedger()
const type = ref<EntryType>('EXPENSE')
const amount = ref('')
const expression = ref('')
const note = ref('')
const accountIndex = ref(0)
const toIndex = ref(1)
const dateTime = ref(localDateTime())
const editingId = ref('')
const saving = ref(false)
const loading = ref(true)
const loadError = ref('')
const refundedCents = ref(0)
const routeId = ref('')
const locked = computed(() => saving.value || loading.value || !!loadError.value)
const modal = ref<'account' | 'to' | 'date' | 'note' | ''>('')
const formError = ref('')
const draftDate = ref('')
const draftTime = ref('')
const draftNote = ref('')
const draftAccountIndex = ref(0)
const dateTimePicker = ref<'date' | 'time' | ''>('')
const initialValue = ref('')
const allowBack = ref(false)
const discardOpen = ref(false)
const successVisible = ref(false)
const createIdempotencyKey = () => `app-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
const idempotencyKey = ref(createIdempotencyKey())
const snapshot = () => JSON.stringify([type.value, expression.value, note.value, accountIndex.value, toIndex.value, dateTime.value])
function back() {
  if (saving.value) return
  if (initialValue.value && initialValue.value !== snapshot()) {
    discardOpen.value = true
  } else { allowBack.value = true; backToLedger() }
}
function closeDiscard() { if (!saving.value) discardOpen.value = false }
function confirmDiscard() { if (saving.value) return; discardOpen.value = false; allowBack.value = true; backToLedger() }
onBackPress(() => { if (allowBack.value) return false; back(); return true })
function openModal(value: typeof modal.value) {
  if (locked.value) return
  if (refundedCents.value && (value === 'account' || value === 'to')) return
  draftDate.value = dateTime.value.slice(0, 10)
  draftTime.value = dateTime.value.slice(11, 16)
  draftNote.value = note.value
  if (value === 'account') draftAccountIndex.value = accountIndex.value
  if (value === 'to') draftAccountIndex.value = toIndex.value
  modal.value = value
}
function confirmModal() {
  if (modal.value === 'account') accountIndex.value = draftAccountIndex.value
  if (modal.value === 'to') toIndex.value = draftAccountIndex.value
  if (modal.value === 'date') dateTime.value = `${draftDate.value}T${draftTime.value}:00`
  if (modal.value === 'note') note.value = draftNote.value
  modal.value = ''
}
function selectAccount(index: number) {
  if (isSameAccountSelection(index)) return
  if (modal.value === 'to') toIndex.value = index
  else accountIndex.value = index
  modal.value = ''
}
function formatPickerDate(value: string) {
  const [year, month, day] = value.split('-')
  return year && month && day ? `${year}年${month}月${day}日` : value
}
function selectDateTimeValue(value: string) {
  if (dateTimePicker.value === 'date') draftDate.value = value
  if (dateTimePicker.value === 'time') draftTime.value = value
  dateTimePicker.value = ''
}
const accounts = computed(() => ledger.state.accounts.filter(account => account.kind === 'FUND' && account.status === 'ACTIVE'))
const paired = computed(() => type.value === 'TRANSFER' || type.value === 'REPAYMENT')
const targetAccounts = computed(() => type.value === 'REPAYMENT' ? ledger.state.accounts.filter(account => account.kind === 'CREDIT' && account.status === 'ACTIVE') : accounts.value)
const pickerAccounts = computed(() => modal.value === 'to' ? targetAccounts.value : accounts.value)
function isSameAccountSelection(index: number) {
  const candidate = pickerAccounts.value[index]
  if (!candidate) return true
  if (modal.value === 'to') return candidate.id === accounts.value[accountIndex.value]?.id
  return paired.value && candidate.id === targetAccounts.value[toIndex.value]?.id
}
const isEdit = computed(() => Boolean(editingId.value))
const typeOptions = computed<Array<{ value: EntryType; label: string }>>(() => type.value === 'REPAYMENT' ? [{ value: 'REPAYMENT', label: '还款' }] : [{ value: 'EXPENSE', label: '支出' }, { value: 'INCOME', label: '收入' }, { value: 'TRANSFER', label: '转账' }])
const typeLabel = computed(() => ({ EXPENSE: '支出', INCOME: '收入', TRANSFER: '转账', REPAYMENT: '还款' }[type.value]))

function setType(value: EntryType) { if (!locked.value && !isEdit.value && !refundedCents.value) type.value = value }
function updateAmount() {
  const completedExpression = expression.value.replace(/[+−×÷]+$/, '').replace(/\.$/, '')
  if (!completedExpression) { amount.value = ''; return }
  try { amount.value = calculateAmount(completedExpression) } catch { amount.value = '' }
}
function appendKey(key: string) {
  if (locked.value) return
  formError.value = ''
  if (key === 'C') { expression.value = ''; amount.value = ''; return }
  if (key === '⌫') { expression.value = expression.value.slice(0, -1); updateAmount(); return }
  if (key === '=') { try { calculate() } catch (error) { formError.value = error instanceof Error ? error.message : '计算失败' }; return }
  const operators = new Set(['+', '−', '×', '÷'])
  const last = expression.value.slice(-1)
  if (operators.has(key) && (!expression.value || operators.has(last))) return
  if (key === '.' && expression.value.split(/[+−×÷]/).pop()?.includes('.')) return
  if (key === '.' && (!expression.value || operators.has(last))) expression.value += '0'
  const operand = expression.value.split(/[+−×÷]/).pop() || ''
  if (/\d/.test(key) && operand.includes('.') && operand.split('.')[1].length >= 2) return
  if (expression.value.length >= 32) return
  expression.value += key
  updateAmount()
}
function calculate() { amount.value = calculateAmount(expression.value) }
function showSuccess(afterSave: 'home' | 'again') {
  successVisible.value = true
  setTimeout(() => {
    successVisible.value = false
    if (afterSave === 'home') uni.switchTab({ url: '/pages/index/index' })
  }, 600)
}
async function loadForEdit(id: string) {
  await ledger.refresh()
  const result = await request<{ refundedCents: number; transaction: { type: EntryType; originalAmountCents: number; accountId?: string; fromAccountId?: string; toAccountId?: string; occurredAt: string; note?: string } }>(`/api/app/transactions/${id}`)
  const transaction = result.transaction
  if (!['EXPENSE', 'INCOME', 'TRANSFER', 'REPAYMENT'].includes(transaction.type)) throw new Error('账单类型不支持')
  editingId.value = id; type.value = transaction.type; amount.value = inputYuan(transaction.originalAmountCents); expression.value = amount.value; note.value = transaction.note || ''; dateTime.value = transaction.occurredAt.slice(0, 16)
  refundedCents.value = result.refundedCents
  const from = transaction.fromAccountId ?? transaction.accountId
  accountIndex.value = accounts.value.findIndex(account => account.id === from)
  toIndex.value = targetAccounts.value.findIndex(account => account.id === transaction.toAccountId)
}
async function initialize() {
  loading.value = true
  loadError.value = ''
  try {
    if (routeId.value) await loadForEdit(routeId.value)
    else {
      await ledger.refresh()
      const lastId = stringId(uni.getStorageSync('last-entry-account'))
      accountIndex.value = Math.max(0, accounts.value.findIndex(account => account.id === lastId))
      toIndex.value = accounts.value.findIndex((account, index) => index !== accountIndex.value)
    }
    initialValue.value = snapshot()
  } catch (error) { loadError.value = error instanceof Error ? error.message : '加载失败，请重试' }
  finally { loading.value = false }
}
onLoad(query => {
  routeId.value = stringId(query?.id)
  editingId.value = routeId.value
  if (!routeId.value && typeof query?.date === 'string' && validLocalDateTime(`${query.date}T12:00`)) dateTime.value = `${query.date}T${localDateTime().slice(11)}`
  void initialize()
})
async function save(afterSave: 'home' | 'again') {
  if (locked.value) return
  saving.value = true
  try {
    formError.value = ''
    if (!validLocalDateTime(dateTime.value)) throw new Error('请选择有效日期和时间')
    if (!expression.value) throw new Error('请输入大于 0 的金额')
    calculate()
    if (Array.from(note.value.trim()).length > 100) throw new Error('备注不能超过100个字符')
    if (cents(amount.value) < refundedCents.value) throw new Error('原始金额不能小于已退款金额')
    const payload: TransactionPayload = { type: type.value, amountCents: cents(amount.value), occurredAt: dateTime.value, note: note.value.trim() || undefined, idempotencyKey: isEdit.value ? undefined : idempotencyKey.value }
    if (paired.value) {
      const from = accounts.value[accountIndex.value]; const to = targetAccounts.value[toIndex.value]
      if (!from || !to) throw new Error(type.value === 'REPAYMENT' ? '请选择资金账户和信贷账户' : '请先创建两个资金账户')
      if (from.id === to.id) throw new Error('转出账户和转入账户不能相同')
      payload.fromAccountId = from.id; payload.toAccountId = to.id
    } else {
      const account = accounts.value[accountIndex.value]
      if (!account) throw new Error('请先创建资金账户')
      payload.accountId = account.id
    }
    const savedAccountId = payload.accountId || payload.fromAccountId
    if (isEdit.value) await ledger.updateTransaction(editingId.value, payload); else await ledger.createTransaction(payload)
    if (savedAccountId) uni.setStorageSync('last-entry-account', savedAccountId)
    if (afterSave === 'again') {
      expression.value = ''
      amount.value = ''
      note.value = ''
      draftNote.value = ''
      editingId.value = ''
      routeId.value = ''
      refundedCents.value = 0
      idempotencyKey.value = createIdempotencyKey()
      initialValue.value = snapshot()
      showSuccess('again')
      return
    }
    allowBack.value = true
    showSuccess('home')
  } catch (error) { formError.value = error instanceof Error ? error.message : '保存失败，请重试'; uni.showToast({ title: formError.value, icon: 'none' }) } finally { if (!allowBack.value) saving.value = false }
}
</script>

<template>
  <view class="page entry-page">
    <view class="entry-content">
      <view class="screen-nav"><button class="back nav-side" aria-label="返回" @click="back">‹</button><text class="page-title">{{ isEdit ? '编辑记账' : '新增记账' }}</text><view class="nav-side" /></view>
      <view v-if="loading" class="list-empty">正在加载账户与账单…</view><view v-else-if="loadError" class="list-empty">{{ loadError }}<button class="text-button" @click="initialize">重新加载</button></view>
      <view v-if="isEdit && !loading && !loadError" :class="['entry-edit-type', type.toLowerCase()]"><text class="entry-edit-type-label">当前账单类型</text><text class="entry-edit-type-value">{{ typeLabel }}记账</text></view>
      <view v-if="!isEdit" :class="['entry-type', { 'single-type': type === 'REPAYMENT' }]"><button v-for="option in typeOptions" :key="option.value" :disabled="locked || !!refundedCents" :class="{ active: type === option.value, 'income-active': type === 'INCOME' && type === option.value, 'transfer-active': type === 'TRANSFER' && type === option.value }" @click="setType(option.value)">{{ option.label }}</button></view>
      <view class="amount-panel"><text class="amount-label">{{ type === 'REPAYMENT' ? '还款金额' : type === 'TRANSFER' ? '转账金额' : '记账金额' }}</text><view class="amount-display"><text :class="['amount-value', { 'amount-placeholder': !amount, 'long-amount': amount.length > 10 }]">{{ amount || '输入金额' }}</text></view><button class="calculation-line" :aria-label="expression ? `当前算式：${expression}` : '支持加减乘除连续计算'" @click="appendKey('=')">{{ expression || '支持 + − × ÷ 连续计算' }}</button></view>
      <view class="fields-card">
        <button class="field-row" :disabled="locked || !!refundedCents" @click="openModal('account')"><text class="field-icon">{{ paired ? '↗' : '◉' }}</text><text class="field-label">{{ type === 'REPAYMENT' ? '还款账户' : type === 'TRANSFER' ? '转出账户' : '资金账户' }}</text><text class="field-value">{{ accounts[accountIndex]?.name || '请选择' }}</text><text class="arrow">›</text></button>
        <button v-if="paired" class="field-row" :disabled="locked || !!refundedCents" @click="openModal('to')"><text class="field-icon">↘</text><text class="field-label">{{ type === 'REPAYMENT' ? '信贷账户' : '转入账户' }}</text><text class="field-value">{{ targetAccounts[toIndex]?.name || '请选择' }}</text><text class="arrow">›</text></button>
        <button class="field-row" :disabled="locked" @click="openModal('date')"><text class="field-icon">◷</text><text class="field-label">日期与时间</text><text class="field-value">{{ dateTime.replace('T', ' ').slice(0, 16) }}</text><text class="arrow">›</text></button>
        <button class="field-row" :disabled="locked" @click="openModal('note')"><text class="field-icon">⌁</text><text class="field-label">备注</text><text :class="['field-value', { placeholder: !note }]">{{ note || '写点说明...' }}</text></button>
      </view>
    </view>
    <view class="keypad" aria-label="金额键盘">
      <view v-for="key in ['1', '2', '3', '⌫', '4', '5', '6', '+−', '7', '8', '9', '×÷', 'C', '0', '.', 'confirm']" :key="key" :class="['keypad-cell', { 'keypad-pair': key === '+−' || key === '×÷' }]">
        <template v-if="key === '+−' || key === '×÷'"><button v-for="operator in key.split('')" :key="operator" class="key" :disabled="locked" @click="appendKey(operator)">{{ operator }}</button></template>
        <button v-else-if="key === 'confirm'" class="key key-confirm" :disabled="locked" @click="save('home')">{{ saving ? '保存中' : '确定' }}</button>
        <button v-else :class="['key', { 'key-text': key === 'C', 'key-backspace': key === '⌫' }]" :aria-label="key === 'C' ? '保存并重新记账' : key === '⌫' ? '退格' : key" :disabled="locked" @click="key === 'C' ? save('again') : appendKey(key)">{{ key === 'C' ? '再记' : key }}</button>
      </view>
    </view>
    <view v-if="successVisible" class="entry-success-toast" role="status" aria-live="polite"><view class="entry-success-icon">✓</view><text>记账成功</text></view>
    <view v-if="discardOpen" class="asset-create-backdrop" @click.self="closeDiscard" @touchmove.stop.prevent>
      <view class="account-delete-modal entry-discard-modal" role="dialog" aria-modal="true" aria-label="放弃修改">
        <view class="asset-create-handle" />
        <text class="account-delete-title">放弃修改？</text>
        <text class="account-delete-copy">当前内容尚未保存，离开后将丢失已修改的内容。</text>
        <view class="account-delete-actions"><button class="account-delete-cancel" :disabled="saving" @click="closeDiscard">继续编辑</button><button class="account-delete-confirm" :disabled="saving" @click="confirmDiscard">放弃修改</button></view>
      </view>
    </view>
    <view v-if="modal === 'account' || modal === 'to'" class="entry-account-picker-backdrop" @click.self="modal = ''" @touchmove.stop.prevent>
      <view class="entry-account-picker-modal" role="dialog" aria-modal="true" :aria-label="modal === 'to' ? (type === 'REPAYMENT' ? '选择信贷账户' : '选择转入账户') : (type === 'TRANSFER' ? '请选择转出账户' : '选择资金账户')">
        <view class="entry-account-picker-handle" />
        <text class="entry-account-picker-title">{{ modal === 'to' ? (type === 'REPAYMENT' ? '选择信贷账户' : '选择转入账户') : (type === 'TRANSFER' ? '请选择转出账户' : '选择资金账户') }}</text>
        <scroll-view scroll-y class="entry-account-choice-list" @touchmove.stop>
          <view v-if="!pickerAccounts.length" class="list-empty">暂无可用账户<button class="text-button" @click="modal = ''; uni.switchTab({ url: '/pages/assets/assets' })">去资产页添加账户</button></view>
          <button v-for="(account, index) in pickerAccounts" v-else :key="account.id" :disabled="isSameAccountSelection(index)" :class="['entry-account-choice-item', { selected: index === draftAccountIndex }]" @click="selectAccount(index)"><image class="entry-account-choice-icon" :src="account.kind === 'CREDIT' ? '/static/prototype/credit-account.png' : '/static/prototype/funds-account.png'" mode="aspectFit" /><text class="entry-account-choice-name">{{ account.name }}</text><MoneyDisplay class="entry-account-choice-balance" :value="account.kind === 'CREDIT' ? -account.balanceCents : account.balanceCents" /><text class="entry-account-choice-state">{{ index === draftAccountIndex ? '✓' : '›' }}</text></button>
        </scroll-view>
        <view class="entry-account-picker-actions"><button class="entry-account-picker-cancel" @click="modal = ''">取消</button><button class="entry-account-picker-confirm" @click="confirmModal">完成</button></view>
      </view>
    </view>
    <view v-else-if="modal === 'date'" class="entry-date-picker-backdrop" @click.self="modal = ''" @touchmove.stop.prevent>
      <view class="entry-date-picker-modal" role="dialog" aria-modal="true" aria-label="日期与时间">
        <view class="entry-date-picker-handle" />
        <text class="entry-date-picker-title">日期与时间</text>
        <view class="entry-date-picker-fields">
          <button class="entry-date-picker-row" @click="dateTimePicker = 'date'"><text>记账日期</text><view class="entry-date-picker-value"><text>{{ formatPickerDate(draftDate) }}</text><text class="entry-date-picker-arrow">›</text></view></button>
          <button class="entry-date-picker-row" @click="dateTimePicker = 'time'"><text>记账时间</text><view class="entry-date-picker-value"><text>{{ draftTime }}</text><text class="entry-date-picker-arrow">›</text></view></button>
        </view>
        <view class="entry-date-picker-actions"><button class="entry-date-picker-cancel" @click="modal = ''">取消</button><button class="entry-date-picker-confirm" @click="confirmModal">确定</button></view>
      </view>
    </view>
    <view v-else-if="modal === 'note'" class="entry-note-picker-backdrop" @click.self="modal = ''" @touchmove.stop.prevent>
      <view class="entry-note-picker-modal" role="dialog" aria-modal="true" aria-label="添加备注">
        <view class="entry-note-picker-handle" />
        <text class="entry-note-picker-title">添加备注</text>
        <textarea v-model="draftNote" class="entry-note-picker-input" maxlength="100" placeholder="记录一点上下文，例如：和朋友聚餐" :show-confirm-bar="false" />
        <text class="entry-note-picker-count">{{ draftNote.length }} / 100</text>
        <view class="entry-note-picker-actions"><button class="entry-note-picker-cancel" @click="modal = ''">取消</button><button class="entry-note-picker-confirm" @click="confirmModal">完成</button></view>
      </view>
    </view>
    <EntryDateTimePicker v-if="dateTimePicker" :mode="dateTimePicker" :value="dateTimePicker === 'date' ? draftDate : draftTime" @close="dateTimePicker = ''" @select="selectDateTimeValue" />
  </view>
</template>
