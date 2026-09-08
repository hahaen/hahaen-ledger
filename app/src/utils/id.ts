export function stringId(value: unknown): string {
  if (Array.isArray(value)) return stringId(value[0])
  return typeof value === 'string' || typeof value === 'number' ? String(value) : ''
}
