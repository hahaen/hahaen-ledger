import type { TodoItem, TodoRepeatFields } from './api'

export const repeatUnits = [{ value: 'DAY', label: '天' }, { value: 'WEEK', label: '周' }, { value: 'MONTH', label: '月' }, { value: 'YEAR', label: '年' }] as const
export const repeatModes = [{ value: 'TIME', label: '按时间' }, { value: 'AFTER_COMPLETION', label: '完成后' }, { value: 'FIXED_DATES', label: '固定日' }] as const
export const splitSelection = (value?: string | null) => value ? value.split(',') : []
export function cleanRepeat(value: TodoRepeatFields): TodoRepeatFields {
  if (value.repeatMode === 'FIXED_DATES') return { repeatMode: value.repeatMode, fixedDates: value.fixedDates || '' }
  const time = value.repeatMode === 'TIME'
  return { repeatMode: value.repeatMode, repeatUnit: value.repeatUnit, repeatInterval: value.repeatInterval,
    weekDays: time && value.repeatUnit === 'WEEK' ? value.weekDays || '' : null,
    monthDays: time && value.repeatUnit === 'MONTH' ? value.monthDays || '' : null,
    lastDay: time && value.repeatUnit === 'MONTH' && Boolean(value.lastDay),
    yearDays: time && value.repeatUnit === 'YEAR' ? value.yearDays || '' : null }
}
export function repeatError(value: TodoRepeatFields): string {
  if (value.repeatMode === 'FIXED_DATES') {
    const dates = splitSelection(value.fixedDates)
    return !dates.length || dates.length > 100 ? '请选择1至100个固定日期' : ''
  }
  if (!Number.isInteger(value.repeatInterval) || Number(value.repeatInterval) < 1 || Number(value.repeatInterval) > 365) return '重复间隔须为1至365'
  if (value.repeatMode !== 'TIME') return ''
  if (value.repeatUnit === 'WEEK' && !value.weekDays) return '请至少选择一个星期'
  if (value.repeatUnit === 'MONTH' && !value.monthDays && !value.lastDay) return '请选择月内日期或每月最后一天'
  if (value.repeatUnit === 'YEAR' && !value.yearDays) return '请至少选择一个年度月日'
  return ''
}
const shortSelection = (values: string[]) => values.length > 3 ? `${values.slice(0, 3).join('、')}等${values.length}个` : values.join('、')
export function recurrenceLabel(item: TodoItem): string {
  if (item.recurrence === 'ONCE') return '仅一次'
  if (item.recurrence === 'DAILY') return '每天'
  const day = Number(item.anchorAt.slice(8, 10))
  if (item.recurrence === 'MONTHLY') return `每月 ${day} 日`
  if (item.recurrence === 'EVERY_N_MONTHS') return `每隔 ${item.monthInterval} 月 ${day} 日`
  if (item.recurrence === 'YEARLY') return `每年 ${Number(item.anchorAt.slice(5, 7))} 月 ${day} 日`
  if (item.repeatMode === 'FIXED_DATES') return `固定日 · ${splitSelection(item.fixedDates).length}个日期`
  const unit = repeatUnits.find(unit => unit.value === item.repeatUnit)?.label || '天'
  const prefix = item.repeatMode === 'AFTER_COMPLETION' ? '完成后每' : '每'
  const selected = item.repeatUnit === 'WEEK' && item.weekDays ? ` · 周${splitSelection(item.weekDays).map(d => ['一', '二', '三', '四', '五', '六', '日'][Number(d) - 1]).join('、')}`
    : item.repeatUnit === 'MONTH' ? ` · ${[item.monthDays && `${shortSelection(splitSelection(item.monthDays))}日`, item.lastDay && '月末'].filter(Boolean).join('、')}`
    : item.repeatUnit === 'YEAR' && item.yearDays ? ` · ${shortSelection(splitSelection(item.yearDays))}` : ''
  return `${prefix}${item.repeatInterval}${unit}${item.repeatMode === 'AFTER_COMPLETION' ? '' : selected}`
}
export function toggleSelection(csv: string | null | undefined, value: string, numeric = false): string {
  const values = new Set(splitSelection(csv))
  if (values.has(value)) values.delete(value)
  else values.add(value)
  return [...values].sort(numeric ? (a, b) => Number(a) - Number(b) : undefined).join(',')
}
