<script setup lang="ts">
import { computed } from 'vue'
import { Transaction, useLedger } from '../stores/ledger'
import { yuan } from '../utils/money'
const props = defineProps<{ transaction: Transaction }>()
const emit = defineEmits<{ open: [id: number] }>()
const ledger = useLedger()
const styles = {
  EXPENSE: { label: '支出', image: 'expense-coin.png', sign: '− ' },
  INCOME: { label: '收入', image: 'income-piggy-bank.png', sign: '＋ ' },
  TRANSFER: { label: '转账', image: 'transfer-card.png', sign: '' },
  REPAYMENT: { label: '还款', image: 'repayment-card.png', sign: '' },
}
const kind = computed(() => styles[props.transaction.type])
const description = computed(() => {
  const transaction = props.transaction
  const name = (id?: number) => ledger.state.accounts.find(account => account.id === id)?.name || '历史账户'
  const account = transaction.type === 'TRANSFER' || transaction.type === 'REPAYMENT'
    ? `${name(transaction.fromAccountId)} → ${name(transaction.toAccountId)}` : name(transaction.accountId)
  return `${transaction.occurredAt.slice(11, 16)} · ${account}`
})
</script>
<template>
  <button class="transaction-item" :aria-label="`查看${kind.label} ${yuan(transaction.amountCents)}详情`" @click="emit('open', transaction.id)">
    <view :class="['type-icon', transaction.type.toLowerCase()]"><image :src="`/static/prototype/${kind.image}`" mode="aspectFit" /></view>
    <view class="transaction-copy"><view class="transaction-title-row"><text :class="['type-label', transaction.type.toLowerCase()]">{{ kind.label }}</text><text v-if="transaction.hasRefund" class="refund-mark">退</text></view><text class="transaction-note">{{ description }}</text></view>
    <text :class="['transaction-amount', transaction.type.toLowerCase()]">{{ kind.sign }}{{ yuan(transaction.amountCents) }}</text><text class="arrow">›</text>
  </button>
</template>
