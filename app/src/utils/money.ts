export type MoneyCents = number | string | null | undefined

export function cents(value: string | number): number { const text = String(value).trim(); if (!/^\d+(\.\d{1,2})?$/.test(text)) throw new Error('金额格式不正确'); const n = Number(text); if (!Number.isFinite(n) || n <= 0 || n > 999999999.99) throw new Error('金额必须在 0.01～999,999,999.99 元之间'); return Math.round(n * 100) }

function normalizeCents(value: MoneyCents): number {
  if (value === null || value === undefined) return 0
  const normalized = Number(String(value).trim())
  return Number.isFinite(normalized) && !Object.is(normalized, -0) ? normalized : 0
}

/** 将整数分格式化为人民币金额文本；整数金额不展示 .00，有效小数保留两位。 */
export function formatYuan(value: MoneyCents): string {
  const formatted = (normalizeCents(value) / 100).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  return formatted.endsWith('.00') ? formatted.slice(0, -3) : formatted
}

export function localDateTime(): string { const d = new Date(); const pad = (x: number) => String(x).padStart(2, '0'); return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00` }
