import { readFileSync } from 'node:fs'
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { parse, compileTemplate } from 'vue/compiler-sfc'
const source = readFileSync(new URL('../src/components/CenterModal.vue', import.meta.url), 'utf8')
const { descriptor } = parse(source)
const { ast, errors } = compileTemplate({source:descriptor.template.content,filename:'CenterModal.vue',id:'item-modal'})
assert.deepEqual(errors, [])
const root = ast.children.find(node => node.tag === 'view')
const events = node => node.props.filter(prop => prop.type === 7 && prop.name === 'on')
// 使用真实模板 AST 检查原生小程序事件边界，阻止恢复会误关编辑器的祖先事件。
test('物品弹窗内容祖先不绑定关闭或禁止触摸，点击关闭仅在独立遮罩', () => {
  assert.deepEqual(events(root), [])
  const mask = root.children.find(node => node.tag === 'view' && node.props.some(prop=>prop.name==='class' && prop.value?.content==='picker-touch-mask'))
  assert.ok(mask)
  const click = events(mask).find(prop=>prop.arg.content==='click')
  assert.match(click.exp.loc.source, /closeOnBackdrop && emit\('close'\)/)
  assert.deepEqual(click.modifiers,['stop'])
  const touch = events(mask).find(prop=>prop.arg.content==='touchmove')
  assert.deepEqual(touch.modifiers,['stop','prevent'])
})
