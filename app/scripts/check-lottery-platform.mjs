import assert from 'node:assert/strict'
import { readFileSync, readdirSync } from 'node:fs'
import { resolve, join, relative } from 'node:path'

const root = resolve(import.meta.dirname, '../dist/build')
const mp = join(root, 'mp-weixin')
const h5 = join(root, 'h5')
const routes = ['lottery', 'lottery-editor', 'lottery-detail', 'lottery-draws'].map(name => `pages/${name}/${name}`)
const config = JSON.parse(readFileSync(join(mp, 'app.json'), 'utf8'))
assert.ok(config.pages.includes('pages/mine/mine'))
assert.ok(config.pages.includes('pages/ha-todo/ha-todo'))
assert.ok(config.pages.every(page => !page.includes('lottery')))

function files(dir) {
  return readdirSync(dir, { withFileTypes: true }).flatMap(entry => {
    const path = join(dir, entry.name)
    return entry.isDirectory() ? files(path) : [path]
  })
}

const mpFiles = files(mp)
for (const path of mpFiles) {
  assert.doesNotMatch(relative(mp, path), /lottery/i, '微信产物不应包含彩票页面/组件/工具文件')
  if (/\.(js|json|wxml|wxss|map)$/.test(path)) {
    assert.doesNotMatch(readFileSync(path, 'utf8'), /lottery|彩票|双色球|大乐透/i, `微信产物残留：${relative(mp, path)}`)
  }
}
const h5Js = files(h5).filter(path => path.endsWith('.js')).map(path => readFileSync(path, 'utf8')).join('\n')
for (const route of routes) assert.ok(h5Js.includes(route), `H5 必须保留路由 ${route}`)
assert.match(h5Js, /\/api\/app\/lottery/)
assert.match(h5Js, /彩票/)
assert.ok(files(h5).filter(path => path.endsWith('.css')).some(path => readFileSync(path, 'utf8').includes('.lottery-page')))
console.log(`PASS：微信 ${config.pages.length} 个页面、${mpFiles.length} 个产物文件无彩票功能；H5 保留四个路由、入口、API 和样式`)
