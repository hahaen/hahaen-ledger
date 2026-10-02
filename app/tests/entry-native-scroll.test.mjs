import { readFileSync } from 'node:fs'
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { parse, compileTemplate } from 'vue/compiler-sfc'
const read = path => readFileSync(new URL(path, import.meta.url), 'utf8')
const { ast: template, errors } = compileTemplate({ source: parse(read('../src/pages/entry/entry.vue')).descriptor.template.content, filename: 'entry.vue', id: 'entry-scroll' })
assert.deepEqual(errors, [])
const nodes = []
function visit(node) { if (node.type === 1) nodes.push(node); for (const child of node.children || []) visit(child); for (const branch of node.branches || []) visit(branch) }
visit(template)
const classes = node => node.props.find(p => p.type === 6 && p.name === 'class')?.value?.content.split(' ') || []
test('新增和编辑主体使用原生纵向滚动容器，祖先不拦截微信手势', () => {
  const body = nodes.find(node => classes(node).includes('entry-content'))
  assert.equal(body.tag, 'scroll-view', '普通 view 的 overflow 不构成微信原生滚动区')
  assert.ok(body.props.some(p => p.name === 'scroll-y'))
  assert.ok(!body.props.some(p => p.type === 7 && p.arg?.content === 'touchmove'), 'scroll-view 不绑定 catchtouchmove')
  const page = nodes.find(node => classes(node).includes('entry-page'))
  assert.ok(!page.props.some(p => p.type === 7 && p.arg?.content === 'touchmove'))
})
test('账户弹窗滚动区的祖先不拦截微信手势', () => {
  const backdrop = nodes.find(node => classes(node).includes('entry-account-picker-backdrop'))
  assert.ok(!backdrop.props.some(p => p.type === 7 && p.arg?.content === 'touchmove'))
  const list = nodes.find(node => classes(node).includes('entry-account-choice-list'))
  assert.ok(!list.props.some(p => p.type === 7 && p.arg?.content === 'touchmove'))
})

test('H5 不安装全局取消 touchmove 的页面禁滚监听', () => {
  const page = JSON.parse(read('../src/pages.json')).pages.find(page => page.path === 'pages/entry/entry')
  assert.notEqual(page.style.disableScroll, true)
})
