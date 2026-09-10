/** 独立视觉验收服务器：只读示例数据，不连接真实后端，也不进入构建产物。 */
import { createServer } from 'node:http'
import { readFile } from 'node:fs/promises'
import { fileURLToPath } from 'node:url'
import { resolve, extname, sep } from 'node:path'

const root = fileURLToPath(new URL('../dist/build/h5/', import.meta.url))
const prototype = fileURLToPath(new URL('../../哈记账小程序_原型设计稿/', import.meta.url))
const port = 18761
const accounts = [
  { id: 1, name: '银行卡', kind: 'FUND', balanceCents: 860000, creditLimitCents: 0, includedInNetAsset: true, status: 'ACTIVE' },
  { id: 2, name: '微信', kind: 'FUND', balanceCents: 428000, creditLimitCents: 0, includedInNetAsset: true, status: 'ACTIVE' },
  { id: 3, name: '现金', kind: 'FUND', balanceCents: 232000, creditLimitCents: 0, includedInNetAsset: true, status: 'ACTIVE' },
  { id: 4, name: '花呗', kind: 'CREDIT', balanceCents: 252000, creditLimitCents: 1000000, includedInNetAsset: true, status: 'ACTIVE' },
].map(row => ({ ...row, id: String(row.id), sortOrder: row.id }))
const transactions = [
  { id: 6, type: 'REPAYMENT', amountCents: 20000, fromAccountId: 1, toAccountId: 4, occurredAt: '2026-09-14T10:00:00', note: '还款编辑验收' },
  { id: 1, type: 'TRANSFER', amountCents: 10000, fromAccountId: 2, toAccountId: 1, occurredAt: '2026-09-15T15:30:00', note: '日常资金转入' },
  { id: 2, type: 'EXPENSE', amountCents: 3200, accountId: 2, occurredAt: '2026-09-15T12:20:00', note: '午餐' },
  { id: 3, type: 'EXPENSE', amountCents: 600, accountId: 3, occurredAt: '2026-09-15T08:41:00', note: '地铁出行' },
  { id: 4, type: 'EXPENSE', amountCents: 8990, accountId: 2, occurredAt: '2026-09-14T18:26:00', note: '晚餐' },
  { id: 5, type: 'INCOME', amountCents: 850000, accountId: 1, occurredAt: '2026-09-14T09:00:00', note: '9月工资' },
].map(row => ({ ...row, id: String(row.id), accountId: row.accountId == null ? undefined : String(row.accountId), fromAccountId: row.fromAccountId == null ? undefined : String(row.fromAccountId), toAccountId: row.toAccountId == null ? undefined : String(row.toAccountId), originalAmountCents: row.amountCents, hasRefund: false, transactionNo: `VISUAL-${row.id}`, status: 'ACTIVE' }))
const totals = rows => {
  const sum = type => rows.filter(row => row.type === type).reduce((total, row) => total + row.amountCents, 0)
  return { expenseCents: sum('EXPENSE'), incomeCents: sum('INCOME'), balanceCents: sum('INCOME') - sum('EXPENSE') }
}
const contentTypes = { '.html': 'text/html', '.js': 'text/javascript', '.css': 'text/css', '.png': 'image/png', '.svg': 'image/svg+xml' }
const server = createServer(async (req, res) => {
  const url = new URL(req.url, `http://127.0.0.1:${port}`)
  const pathname = decodeURIComponent(url.pathname)
  const json = (data, status = 200) => { res.writeHead(status, { 'Content-Type': 'application/json' }); res.end(JSON.stringify({ code: status === 200 ? 0 : status, message: status === 200 ? '视觉验收数据' : '视觉验收环境不执行写入', data })) }
  if (pathname.startsWith('/api/')) {
    if (req.method !== 'GET') { json(null, 503); return }
    if (pathname === '/api/app/home/summary') { json({ month: url.searchParams.get('month'), dailyExpenseCents: 8640, expenseCents: 259200, incomeCents: 850000, balanceCents: 590800, transactions }); return }
    if (pathname === '/api/app/accounts') { json(accounts); return }
    if (pathname === '/api/app/assets/overview') { json({ totalAssetsCents: 1520000, totalLiabilitiesCents: 252000, netAssetsCents: 1268000, accounts }); return }
    if (pathname === '/api/app/user/profile') { json({ userId: 1, nickname: '账本主人', cumulativeDays: 28, avatarAuthorized: false }); return }
    if (pathname === '/api/app/calendar') {
      const year = Number(url.searchParams.get('year')), month = Number(url.searchParams.get('month'))
      const first = new Date(year, month - 1, 1), start = new Date(year, month - 1, 1 - first.getDay())
      const days = Array.from({ length: 42 }, (_, index) => {
        const day = new Date(start.getFullYear(), start.getMonth(), start.getDate() + index)
        const date = `${day.getFullYear()}-${String(day.getMonth() + 1).padStart(2, '0')}-${String(day.getDate()).padStart(2, '0')}`
        const rows = transactions.filter(row => row.occurredAt.startsWith(date))
        return { date, day: day.getDate(), currentMonth: day.getMonth() === month - 1, today: false, hasRecords: rows.length > 0, ...totals(rows) }
      })
      json({ month: `${year}-${String(month).padStart(2, '0')}`, days }); return
    }
    if (pathname.startsWith('/api/app/calendar/')) { const date = pathname.split('/').pop(), rows = transactions.filter(row => row.occurredAt.startsWith(date)); json({ date, ...totals(rows), transactions: rows }); return }
    if (/^\/api\/app\/transactions\/\d+$/.test(pathname)) { json({ transaction: transactions.find(row => row.id === pathname.split('/').pop()), refundedCents: 0, effectiveCents: transactions.find(row => row.id === pathname.split('/').pop())?.amountCents, refunds: [] }); return }
    if (/^\/api\/app\/accounts\/\d+\/transactions$/.test(pathname)) { const id = pathname.split('/')[4]; const rows = transactions.filter(row => [row.accountId, row.fromAccountId, row.toAccountId].includes(id) && (!url.searchParams.get('type') || row.type === url.searchParams.get('type'))); json({ items: rows, total: rows.length, page: 1, pageSize: 20 }); return }
    if (/^\/api\/app\/accounts\/\d+$/.test(pathname)) { json(accounts.find(row => row.id === pathname.split('/').pop())); return }
    json(null, 404); return
  }
  try {
    const reference = pathname.startsWith('/reference/')
    const base = reference ? prototype : root
    const relative = reference ? pathname.slice('/reference/'.length) || 'index.html' : pathname === '/' ? 'index.html' : pathname.slice(1)
    const path = pathname === '/哈记账.png' ? resolve(root, 'static/brand.png') : resolve(base, relative)
    if (!path.startsWith(resolve(base) + sep) && pathname !== '/哈记账.png') { res.writeHead(403).end(); return }
    let data = await readFile(path)
    const extension = extname(path)
    if (!reference && extension === '.js') data = Buffer.from(data.toString().replaceAll('http://127.0.0.1:8080', `http://127.0.0.1:${port}`))
    if (!reference && extension === '.html') data = Buffer.from(data.toString().replace('<head>', `<head><script>localStorage.setItem('auth-token','visual-fixture-only');</script>`))
    res.writeHead(200, { 'Content-Type': contentTypes[extension] || 'application/octet-stream', 'Cache-Control': 'no-store', 'Content-Security-Policy': "connect-src 'self'" })
    res.end(data)
  } catch { res.writeHead(404).end('未找到视觉验收资源') }
})
server.listen(port, '127.0.0.1', () => process.stdout.write(`独立视觉验收：http://127.0.0.1:${port}（只读，不连接真实后端）\n`))
