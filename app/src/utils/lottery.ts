export type LotteryGame = 'DLT' | 'SSQ'
export type LotteryKind = 'single' | 'plan'
export type LotteryDetailKind = 'entry' | 'plan' | 'batch' | 'draw'
export type LotterySummary = { costCents: string | number; settledCostCents: string | number; waitingCostCents: string | number; prizeCents: string | number; profitCents: string | number; pendingCount: number; entryCount: number; drawnCount?: number; remainingCount?: number }
export type LotterySync = { lastSuccessAt?: string; lastError?: string; historyComplete?: boolean; historyPage?: number }
export type LotteryDrawSummary = { costCents: string; prizeCents: string | null; entryCount: number; pendingCount: number }
export type LotteryDelivery = { id: string; channel: string; status: string; attempts: number; acceptedAt?: string }
export type LotteryEvent = { id: string; kind: string; title: string; body: string; deliveries: LotteryDelivery[] }
export type LotteryPlanProgress = {
  batchId: string | null; startIssue: string | null; periods: number; drawnCount: number; remainingCount: number
  estimatedReminderDate: string | null
  reminderState: 'NO_PURCHASE' | 'PAUSED' | 'ENDED' | 'NO_CHANNEL' | 'ACCEPTED' | 'DUE' | 'FAILED' | 'UNKNOWN' | 'UNCONFIRMED' | 'WAITING_SYNC' | 'WAITING'
}
export type LotteryRecord = {
  id: string; game: LotteryGame; name?: string; issue?: string | null; startIssue?: string; red?: string; blue?: string
  multiple?: number; additional?: number; purchasedOn?: string; note?: string; status?: string
  periods?: number; position?: number; planId?: string; batchId?: string; costCents?: string; prizeCents?: string | null
  result?: string; grade?: string; acceptedAt?: string; editable?: boolean; awaitingRenewal?: boolean
  summary?: LotterySummary; draw?: LotteryRecord; drawDate?: string; source?: string; syncedAt?: string
  progress?: LotteryPlanProgress
  personalSummary?: LotteryDrawSummary
  ruleVersion?: string; version?: number; prizes?: string; specialState?: string; promotion?: number
  items?: LotteryRecord[]; hasMore?: boolean; notificationConfigured?: boolean; notifications?: LotteryEvent[]
}
export type LotteryPage = { items: LotteryRecord[]; hasMore: boolean; summary: LotterySummary; notificationConfigured: boolean; sync: LotterySync }
export type LotteryPayload = { game: LotteryGame; name: string; red: string; blue: string; multiple: number; additional: boolean; issue: string; periods: number; purchasedOn: string; note: string; confirmedPurchase: boolean; idempotencyKey: string }
export const gameName = (game: LotteryGame) => game === 'DLT' ? '大乐透' : '双色球'
export const resultLabel = (record: LotteryRecord) => record.result === 'SETTLED' ? record.grade || '已结算' : record.result === 'REVIEW' ? record.grade || '待核实' : record.issue ? '待开奖' : '期号待核对'
export const planLabel = (record: LotteryRecord) => record.status === 'PAUSED' ? '已暂停' : record.status === 'ENDED' ? '已结束' : record.awaitingRenewal ? '待续买' : '进行中'
export const deliveryLabel = (value: string) => ({ PENDING: '待发送', SENDING: '发送中', ACCEPTED: '渠道已受理', FAILED: '发送失败', UNKNOWN: '发送结果未知', CANCELLED: '已取消' }[value] || value)
export function reminderLabel(progress?: LotteryPlanProgress) {
  if (!progress) return '进度暂不可用，请刷新重试'
  const labels: Partial<Record<LotteryPlanProgress['reminderState'], string>> = {
    NO_PURCHASE: '登记实际购买后计算', PAUSED: '已暂停提醒', ENDED: '计划已结束，无后续提醒',
    NO_CHANNEL: '请先配置通知渠道', ACCEPTED: '本批到期提醒已受理，待续买',
    DUE: '本批已到期，等待发送提醒', FAILED: '到期提醒失败，请进入批次重试',
    UNKNOWN: '到期提醒发送结果未知，请查看批次', UNCONFIRMED: '待官方期次核对',
  }
  if (labels[progress.reminderState]) return labels[progress.reminderState]
  if (!progress.estimatedReminderDate) return '待官方期次核对'
  return `${progress.estimatedReminderDate} 晚开奖后${progress.reminderState === 'WAITING_SYNC' ? '（等待开奖数据同步）' : ''}`
}
export function today() { return new Date(Date.now() + 8 * 60 * 60 * 1000).toISOString().slice(0, 10) }
export function normalizeNumbers(value: string, count: number, max: number) {
  const parts = value.trim().split(/[,，\s]+/)
  if (parts.length !== count || parts.some(p => !/^\d{1,2}$/.test(p))) throw new Error(`请填写${count}个号码，以空格或逗号分隔`)
  const numbers = parts.map(Number)
  if (new Set(numbers).size !== count || numbers.some(n => n < 1 || n > max)) throw new Error(`号码须在01～${max}之间且不能重复`)
  return numbers.sort((a, b) => a - b).map(n => String(n).padStart(2, '0')).join(',')
}
export function validateLottery(payload: LotteryPayload, mode: string) {
  const dlt = payload.game === 'DLT'
  payload.red = normalizeNumbers(payload.red, dlt ? 5 : 6, dlt ? 35 : 33)
  payload.blue = normalizeNumbers(payload.blue, dlt ? 2 : 1, dlt ? 12 : 16)
  if (!Number.isInteger(payload.multiple) || payload.multiple < 1 || payload.multiple > 99) throw new Error('倍数须为1～99的整数')
  if (!dlt && payload.additional) throw new Error('双色球不支持追加')
  if (mode !== 'plan' && !(mode === 'entry' && !payload.issue) && !(dlt ? /^\d{5}$/ : /^\d{7}$/).test(payload.issue)) throw new Error(dlt ? '大乐透期号须为5位' : '双色球期号须为7位')
  if (!Number.isInteger(payload.periods) || payload.periods < 1 || payload.periods > 20) throw new Error('每批须为1～20期')
  if (!payload.purchasedOn || payload.purchasedOn > today()) throw new Error('购买日期不能晚于今天')
  if (mode === 'plan' && !payload.name.trim()) throw new Error('请填写计划名称')
  if (payload.name.length > 60 || payload.note.length > 500) throw new Error('名称或备注过长')
  if ((mode === 'single' || mode === 'purchase') && !payload.confirmedPurchase) throw new Error('请确认已经实际购买')
  return payload
}
