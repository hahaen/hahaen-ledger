import { readFileSync } from 'node:fs'
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { parse } from 'vue/compiler-sfc'

const { descriptor, errors } = parse(readFileSync(new URL('../src/pages/detail/detail.vue', import.meta.url), 'utf8'))
assert.deepEqual(errors, [])
const classes = node => node.props?.find(prop => prop.name === 'class')?.value?.content.split(' ') || []
const event = (node, name) => node.props?.find(prop => prop.type === 7 && prop.name === 'on' && prop.arg?.content === name)
function find(node, predicate, parents = []) {
  if (predicate(node)) return { node, parents }
  for (const child of node.children || []) {
    const result = find(child, predicate, [...parents, node])
    if (result) return result
  }
}

test('退款金额输入框及祖先不绑定点击关闭或取消默认触摸', () => {
  const { node: modal } = find(descriptor.template.ast, node => classes(node).includes('refund-modal'))
  const { node: input } = find(modal, node => node.tag === 'input')
  const { parents } = find(descriptor.template.ast, node => node === input)
  for (const ancestor of [input, ...parents]) {
    assert.ok(!event(ancestor, 'click'), '微信金额输入不能冒泡到关闭事件')
    const touch = event(ancestor, 'touchmove')
    assert.ok(!touch?.modifiers.includes('prevent') && !touch?.modifiers.includes('stop'))
  }
})

test('退款关闭只在独立同级遮罩，提交中保留禁止关闭保护', () => {
  const { node } = find(descriptor.template.ast, node => classes(node).includes('refund-backdrop'))
  const mask = node.children.find(child => classes(child).includes('picker-touch-mask'))
  assert.ok(mask, '遮罩必须与输入面板同级')
  assert.deepEqual(event(mask, 'click').modifiers, ['stop'])
  assert.match(event(mask, 'click').exp.content, /!saving && \(refundOpen = false\)/)
  assert.deepEqual(event(mask, 'touchmove').modifiers, ['stop', 'prevent'])
})
