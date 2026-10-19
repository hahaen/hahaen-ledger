import { readFileSync } from 'node:fs'
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { parse, compileTemplate } from 'vue/compiler-sfc'

const source = readFileSync(new URL('../src/pages/account/account.vue', import.meta.url), 'utf8')
const { ast, errors } = compileTemplate({ source: parse(source).descriptor.template.content, filename: 'account.vue', id: 'repayment-scroll' })
assert.deepEqual(errors, [])
const classes = node => node.props?.find(prop => prop.name === 'class')?.value?.content.split(' ') || []
function find(node, name, parents = []) {
  if (classes(node).includes(name)) return { node, parents }
  for (const child of [...(node.children || []), ...(node.branches || [])]) {
    const found = find(child, name, [...parents, node])
    if (found) return found
  }
}
const on = (node, name) => node.props?.find(prop => prop.type === 7 && prop.name === 'on' && prop.arg?.content === name)

test('还款账户使用原生纵向列表，列表及祖先不取消触摸默认行为', () => {
  const { node, parents } = find(ast, 'repay-choice-list')
  assert.equal(node.tag, 'scroll-view')
  assert.ok(node.props.some(prop => prop.name === 'scroll-y'))
  for (const ancestor of [node, ...parents]) {
    const touch = on(ancestor, 'touchmove')
    assert.ok(!touch?.modifiers.includes('prevent'))
    assert.ok(!touch?.modifiers.includes('stop'), '小程序祖先不能生成 catchtouchmove')
  }
})

test('关闭及禁止背景滑动仅绑定在独立遮罩，内容点击不会误关弹窗', () => {
  const { node } = find(ast, 'repay-picker-backdrop')
  assert.equal(on(node, 'click'), undefined)
  assert.equal(on(node, 'touchmove'), undefined)
  const mask = node.children.find(child => classes(child).includes('picker-touch-mask'))
  assert.deepEqual(on(mask, 'touchmove').modifiers, ['stop', 'prevent'])
  assert.match(on(mask, 'click').exp.loc.source, /repayAccountOpen = false/)
})
