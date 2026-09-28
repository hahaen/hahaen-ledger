export type Item = {
  id: string; name: string; priceCents: number; purchasedOn: string
  status: 'ACTIVE' | 'RETIRED'; retiredOn: string | null; resaleCents: number | null
  serviceDays: number; netCostCents: number; dailyCostCents: number
}
export type CostPoint = { date: string; day: number; dailyCostCents: number }
export type ItemDetail = { item: Item; asOf: string; costHistory: CostPoint[] }
export type ItemOverview = {
  asOf: string; totalAssetsCents: number; totalDailyCostCents: number
  activeCount: number; retiredCount: number; items: Item[]; total: number
  page: number; pageSize: number; hasMore: boolean
}
export type ItemPayload = {
  name: string; priceCents: number; purchasedOn: string; serving: boolean
  retiredOn: string | null; resaleCents: number | null; idempotencyKey: string
}
export type RetirePayload = { retiredOn: string; resaleCents: number; idempotencyKey: string }
export function itemToday(): string {
  const date = new Date(Date.now() + 8 * 60 * 60 * 1000)
  return date.toISOString().slice(0, 10)
}
export function itemAmount(value: string): number {
  const text = value.trim()
  if (!/^\d+(\.\d{1,2})?$/.test(text)) throw new Error('请输入正确金额，最多两位小数')
  const [whole, fraction = ''] = text.split('.')
  const cents = Number(whole) * 100 + Number(fraction.padEnd(2, '0'))
  if (!Number.isSafeInteger(cents) || cents > 99_999_999_999) throw new Error('金额不能超过999,999,999.99元')
  return cents
}
export function validItemDate(value: string, today = itemToday()) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value) || value < '1000-01-01' || value > today) throw new Error('日期不能晚于今天')
  const date = new Date(`${value}T00:00:00Z`)
  if (!Number.isFinite(date.getTime()) || date.toISOString().slice(0, 10) !== value) throw new Error('日期格式不正确')
}
/** 相同内容失败重试复用键，修改内容后生成新键。 */
export function itemSubmission() {
  let fingerprint = '', key = ''
  return (payload: object) => {
    const next = JSON.stringify(payload)
    if (next !== fingerprint) {
      fingerprint = next
      key = `item_${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 14)}_${Math.random().toString(36).slice(2, 10)}`
    }
    return { ...payload, idempotencyKey: key }
  }
}
