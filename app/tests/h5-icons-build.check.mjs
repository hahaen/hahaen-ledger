// 先执行 build:h5；验证生产 HTML 在 /haji/ 下引用的实际图标资源。
import assert from 'node:assert/strict'
import { createServer } from 'node:http'
import { readFile } from 'node:fs/promises'
import { extname, resolve, sep } from 'node:path'

const root = resolve(import.meta.dirname, '../dist/build/h5')
const sourceRoot = resolve(import.meta.dirname, '..')
const base = '/haji/'
const types = { '.html': 'text/html', '.png': 'image/png' }
const server = createServer(async (req, res) => {
  const pathname = new URL(req.url, 'http://localhost').pathname
  if (!pathname.startsWith(base)) return res.writeHead(404).end()
  const file = resolve(root, pathname.slice(base.length) || 'index.html')
  if (!file.startsWith(root + sep)) return res.writeHead(403).end()
  try {
    const data = await readFile(file)
    res.writeHead(200, { 'Content-Type': types[extname(file)] || 'application/octet-stream' })
    res.end(data)
  } catch {
    res.writeHead(404).end()
  }
})

await new Promise(resolve => server.listen(0, '127.0.0.1', resolve))
try {
  const page = `http://127.0.0.1:${server.address().port}${base}`
  const response = await fetch(page)
  assert.equal(response.status, 200)
  const html = await response.text()
  const links = [...html.matchAll(/<link\b[^>]*>/gi)].map(([tag]) =>
    Object.fromEntries([...tag.matchAll(/([\w-]+)=["']([^"']*)["']/g)].map(([, key, value]) => [key, value])),
  )
  for (const [rel, source] of [
    ['icon', 'static/brand.png'],
    ['apple-touch-icon', 'offloaded-static-assets/wx/brand-white.png'],
  ]) {
    const icons = links.filter(link => link.rel === rel)
    assert.equal(icons.length, 1, `必须声明唯一的 ${rel}`)
    const url = new URL(icons[0].href, page)
    assert.equal(url.origin, new URL(page).origin, `${rel} 必须随 H5 部署`)
    assert.ok(url.pathname.startsWith(base), `${rel} 缺少部署前缀 ${base}: ${url.pathname}`)
    const icon = await fetch(url)
    assert.equal(icon.status, 200, `${rel} 请求失败: ${url.pathname}`)
    assert.match(icon.headers.get('content-type') || '', /^image\/png\b/, `${rel} 必须返回 PNG，不能返回 HTML`)
    const bytes = Buffer.from(await icon.arrayBuffer())
    assert.deepEqual(bytes.subarray(0, 8), Buffer.from('89504e470d0a1a0a', 'hex'))
    assert.equal(bytes.readUInt32BE(16), bytes.readUInt32BE(20), `${rel} 必须为正方形`)
    assert.ok(bytes.readUInt32BE(16) >= 180, `${rel} 分辨率不足`)
    assert.deepEqual(bytes, await readFile(resolve(sourceRoot, source)), `${rel} 未使用既有品牌图标`)
    console.log(`PASS: ${rel} ${url.pathname} HTTP 200 image/png，与源文件一致`)
  }
} finally {
  await new Promise(resolve => server.close(resolve))
}
