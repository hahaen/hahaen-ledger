import { reactive } from 'vue'
import { request } from '../utils/api'
import { localDateTime } from '../utils/money'

export type AccountKind = 'FUND' | 'CREDIT'
export type Account = {
  id: string
  name: string
  kind: AccountKind
  sortOrder: number
  balanceCents: number
  creditLimitCents: number
  includedInNetAsset: boolean
  status: string
}
export type TransactionType = 'EXPENSE' | 'INCOME' | 'TRANSFER' | 'REPAYMENT'
export type Transaction = {
  id: string
  transactionNo: string
  type: TransactionType
  amountCents: number
  originalAmountCents: number
  hasRefund: boolean
  accountId?: string
  fromAccountId?: string
  toAccountId?: string
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
export type HomeRecentTransactions = {
  startMonth: string
  endMonth: string
  transactions: Transaction[]
  hasMore: boolean
}
export type TransactionPayload = {
  type: TransactionType
  amountCents: number
  occurredAt: string
  accountId?: string
  fromAccountId?: string
  toAccountId?: string
  note?: string
  idempotencyKey?: string
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
  user?: { id: string; nickname: string }
  accounts: Account[]
  transactions: Transaction[]
  summary?: Summary
  loading: boolean
}

const state = reactive<State>({ token: '', accounts: [], transactions: [], loading: false })
let restored = false
let refreshSequence = 0
let pendingRefresh: { month: string; promise: Promise<Summary> } | undefined

export function useLedger() {
  async function restore() {
    if (restored) return
    restored = true
    state.token = uni.getStorageSync('auth-token') || ''
    const storedUser = uni.getStorageSync('auth-user') as { id?: unknown; nickname?: unknown } | undefined
    if (storedUser && (typeof storedUser.id === 'string' || typeof storedUser.id === 'number') && typeof storedUser.nickname === 'string') {
      state.user = { id: String(storedUser.id), nickname: storedUser.nickname }
    }
  }

  async function login() {
    let code = `dev-${Date.now()}`
    // #ifdef MP-WEIXIN
    const wxLogin = await new Promise<UniApp.LoginRes>((resolve, reject) => uni.login({ provider: 'weixin', success: resolve, fail: reject }))
    code = wxLogin.code
    // #endif
    const result = await request<{ token: string; userId: string; nickname: string }>('/api/app/auth/login', { method: 'POST', data: { code } })
    state.token = result.token
    state.user = { id: result.userId, nickname: result.nickname }
    uni.setStorageSync('auth-token', result.token)
    uni.setStorageSync('auth-user', state.user)
    await refresh()
  }

  async function refresh(month = localDateTime().slice(0, 7)) {
    if (pendingRefresh?.month === month) return pendingRefresh.promise
    const sequence = ++refreshSequence
    state.loading = true
    const promise = (async () => {
      try {
        const [summary, accounts] = await Promise.all([
          request<Summary>(`/api/app/home/summary?month=${encodeURIComponent(month)}`),
          request<Account[]>('/api/app/accounts'),
        ])
        if (sequence !== refreshSequence) return summary
        state.summary = summary
        state.transactions = summary.transactions || []
        state.accounts = accounts
        return summary
      } finally {
        if (sequence === refreshSequence) state.loading = false
      }
    })()
    pendingRefresh = { month, promise }
    try { return await promise }
    finally { if (pendingRefresh?.promise === promise) pendingRefresh = undefined }
  }

  async function loadHomeRecentTransactions(beforeMonth?: string) {
    const query = beforeMonth ? `?beforeMonth=${encodeURIComponent(beforeMonth)}` : ''
    return request<HomeRecentTransactions>(`/api/app/home/recent-transactions${query}`)
  }

  async function refreshAfterWrite() {
    try { await refresh(state.summary?.month) }
    catch { uni.showToast({ title: '操作已成功，数据刷新失败，请返回后重试刷新', icon: 'none' }) }
  }

  async function createTransaction(payload: TransactionPayload) {
    await request<Transaction>('/api/app/transactions', { method: 'POST', data: payload })
    await refreshAfterWrite()
  }

  async function updateTransaction(id: string, payload: TransactionPayload) {
    await request<Transaction>(`/api/app/transactions/${id}`, { method: 'PUT', data: payload })
    await refreshAfterWrite()
  }

  async function createAccount(payload: Record<string, unknown>) {
    await request<Account>('/api/app/accounts', { method: 'POST', data: payload })
    await refresh()
  }

  async function reorderAccounts(accountId: string, targetAccountId: string, sortOrder: number, targetSortOrder: number) {
    await request<Account[]>(`/api/app/accounts/${accountId}/order`, {
      method: 'PUT',
      data: {
        targetAccountId,
        expectedSortOrder: sortOrder,
        targetExpectedSortOrder: targetSortOrder,
        idempotencyKey: `account-order-${accountId}-${targetAccountId}-${sortOrder}-${targetSortOrder}`,
      },
    })
    await refresh()
  }

  async function deleteAccount(id: string) {
    await request<void>(`/api/app/accounts/${id}`, { method: 'DELETE' })
    await refresh()
  }

  async function deleteTransaction(id: string) {
    await request<void>(`/api/app/transactions/${id}`, { method: 'DELETE' })
    await refreshAfterWrite()
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

  return { state, restore, login, refresh, loadHomeRecentTransactions, logout, clearSession, createTransaction, updateTransaction, createAccount, reorderAccounts, deleteAccount, deleteTransaction, localDateTime }
}
