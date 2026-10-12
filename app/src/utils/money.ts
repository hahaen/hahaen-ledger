export type MoneyCents = number | string | null | undefined

export function cents(value: string | number): number {
  const text = String(value).trim()
  if (!/^\d+(\.\d{1,2})?$/.test(text)) throw new Error('金额格式不正确')
  const [whole, fraction = ''] = text.split('.')
  const result = Number(whole) * 100 + Number(fraction.padEnd(2, '0'))
  if (!Number.isSafeInteger(result) || result <= 0 || result > 99999999999) throw new Error('金额必须在 0.01～999,999,999.99 元之间')
  return result
}

/** 输入框使用无分组符的金额，不能复用带千分位的展示文本。 */
export function inputYuan(value: number): string {
  const sign = value < 0 ? '-' : ''
  const absolute = Math.abs(value)
  const fraction = absolute % 100
  return `${sign}${Math.trunc(absolute / 100)}${fraction ? '.' + String(fraction).padStart(2, '0') : ''}`
}

function normalizeCents(value: MoneyCents): number {
  if (value === null || value === undefined) return 0
  const normalized = Number(String(value).trim())
  return Number.isFinite(normalized) && !Object.is(normalized, -0) ? normalized : 0
}

/** 按分四舍五入；整数元隐藏 .00，其余保留两位，不依赖平台 Intl 支持。 */
export function formatYuan(value: MoneyCents): string {
  const normalized = normalizeCents(value)
  // 日均成本可含小数分；先舍入分，避免换算元后浮点误差影响半分边界。
  const rounded = Math.round(Math.abs(normalized))
  const sign = normalized < 0 && rounded > 0 ? '-' : ''
  const whole = String(Math.trunc(rounded / 100)).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
  const fraction = rounded % 100
  return `${sign}${whole}${fraction ? '.' + String(fraction).padStart(2, '0') : ''}`
}

export function localDateTime(): string { const d = new Date(); const pad = (x: number) => String(x).padStart(2, '0'); return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00` }
