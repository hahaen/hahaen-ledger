/** 后端无偏移计划时间均为北京时间；天数按剩余/逾期时长向上取整。 */
export function todoDueLabel(dueAt: string, now = Date.now()): string {
  const value = /(?:Z|[+-]\d{2}:?\d{2})$/.test(dueAt) ? dueAt : `${dueAt}+08:00`
  const due = new Date(value).getTime()
  if (!Number.isFinite(due)) return '时间待确认'
  const days = Math.ceil(Math.abs(due - now) / 86400000)
  return `${due < now ? '过期' : '距离'}${days}天`
}
