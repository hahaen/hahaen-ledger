import { reactive } from 'vue'
import { request } from '../utils/api'
import { localDateTime } from '../utils/money'

export type AccountKind = 'FUND' | 'CREDIT'
export type Account = {
  id: number
  name: string
  kind: AccountKind
  balanceCents: number
  creditLimitCents: number
  includedInNetAsset: boolean
  status: string
}
export type TransactionType = 'EXPENSE' | 'INCOME' | 'TRANSFER' | 'REPAYMENT'
export type Transaction = {
  id: number
  transactionNo: string
  type: TransactionType
  amountCents: number
  originalAmountCents: number
  hasRefund: boolean
  accountId?: number
  fromAccountId?: number
  toAccountId?: number
  occurredAt: string
  note?: string
  status: string
}
export type Summary = {
  month: string
  dailyExpenseCents: number
  expenseCents: number
  incomeCents: number
  balanceCents: number
  transactions: Transaction[]
}
export type TransactionPage = { items: Transaction[]; total: number; page: number; pageSize: number }
export type AssetOverview = {
  totalAssetsCents: number
  totalLiabilitiesCents: number
  netAssetsCents: number
  accounts: Account[]
}
type State = {
  token: string
  user?: { id: number; nickname: string }
  accounts: Account[]
  transactions: Transaction[]
  summary?: Summary
  loading: boolean
}

const state = reactive<State>({ token: '', accounts: [], transactions: [], loading: false })
let restored = false
let refreshSequence = 0

export function useLedger() {
  async function restore() {
    if (restored) return
    restored = true
    state.token = uni.getStorageSync('auth-token') || ''
    const storedUser = uni.getStorageSync('auth-user') as { id?: unknown; nickname?: unknown } | undefined
    if (storedUser && typeof storedUser.id === 'number' && typeof storedUser.nickname === 'string') {
      state.user = { id: storedUser.id, nickname: storedUser.nickname }
    }
  }

  async function login() {
    let code = `dev-${Date.now()}`
    // #ifdef MP-WEIXIN
    const wxLogin = await new Promise<UniApp.LoginRes>((resolve, reject) => uni.login({ provider: 'weixin', success: resolve, fail: reject }))
    code = wxLogin.code
    // #endif
    const result = await request<{ token: string; userId: number; nickname: string }>('/api/app/auth/login', { method: 'POST', data: { code } })
    state.token = result.token
    state.user = { id: result.userId, nickname: result.nickname }
    uni.setStorageSync('auth-token', result.token)
    uni.setStorageSync('auth-user', state.user)
    await refresh()
  }

  async function refresh(month = localDateTime().slice(0, 7)) {
    const sequence = ++refreshSequence
    state.loading = true
    try {
      const [summary, accounts] = await Promise.all([
        request<Summary>(`/api/app/home/summary?month=${encodeURIComponent(month)}`),
        request<Account[]>('/api/app/accounts'),
      ])
      if (sequence !== refreshSequence) return
      state.summary = summary
      state.transactions = summary.transactions || []
      state.accounts = accounts
    } finally {
      if (sequence === refreshSequence) state.loading = false
    }
  }

  async function createTransaction(payload: Record<string, unknown>) {
    await request<Transaction>('/api/app/transactions', { method: 'POST', data: payload })
    await refresh()
  }

  async function updateTransaction(id: number, payload: Record<string, unknown>) {
    await request<Transaction>(`/api/app/transactions/${id}`, { method: 'PUT', data: payload })
    await refresh()
  }

  async function createAccount(payload: Record<string, unknown>) {
    await request<Account>('/api/app/accounts', { method: 'POST', data: payload })
    await refresh()
  }

  async function deleteAccount(id: number) {
    await request<void>(`/api/app/accounts/${id}`, { method: 'DELETE' })
    await refresh()
  }

  async function deleteTransaction(id: number) {
    await request<void>(`/api/app/transactions/${id}`, { method: 'DELETE' })
    await refresh()
  }

  function clearSession() {
    state.token = ''
    state.user = undefined
    state.accounts = []
    state.transactions = []
    state.summary = undefined
    uni.removeStorageSync('auth-token')
    uni.removeStorageSync('auth-user')
  }

  async function logout() {
    try {
      await request<void>('/api/app/auth/logout', { method: 'POST' })
    } finally {
      clearSession()
    }
    // #ifdef MP-WEIXIN
    await login()
    uni.reLaunch({ url: '/pages/index/index' })
    // #endif
  }

  return { state, restore, login, refresh, logout, clearSession, createTransaction, updateTransaction, createAccount, deleteAccount, deleteTransaction, localDateTime }
}
